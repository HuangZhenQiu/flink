/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.formats.parquet.utils;

import org.apache.flink.api.common.typeinfo.BasicArrayTypeInfo;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.SqlTimeTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.MapTypeInfo;
import org.apache.flink.api.java.typeutils.ObjectArrayTypeInfo;
import org.apache.flink.api.java.typeutils.RowTypeInfo;

import org.apache.parquet.avro.AvroSchemaConverter;
import org.apache.parquet.schema.GroupType;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.OriginalType;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.Type;
import org.apache.parquet.schema.Types;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Schema converter converts Parquet schema to and from Flink internal types.
 */
public class ParquetSchemaConverter {
	public static final String MAP_KEY = "key";
	public static final String MAP_VALUE = "value";
	public static final String LIST_ARRAY_TYPE = "array";
	public static final String LIST_ELEMENT = "element";
	public static final String LIST_GROUP_NAME = "list";
	public static final String MESSAGE_ROOT = "root";
	private static final AvroSchemaConverter SCHEMA_CONVERTER = new AvroSchemaConverter();

	public static TypeInformation<?> fromParquetType(MessageType type) {
		return convertFields(type.getFields());
	}

	/**
	 * Converts Flink Internal Type to Parquet schema.
	 *
	 * @param typeInformation  flink type information
	 * @param isStandard is standard LIST and MAP schema or back-compatible schema
	 * @return Parquet schema
	 */
	public static MessageType toParquetType(TypeInformation<?> typeInformation, boolean isStandard) {
		return (MessageType) convertField(null, typeInformation, Type.Repetition.OPTIONAL, isStandard);
	}

	private static TypeInformation<?> convertFields(List<Type> parquetFields) {
		List<TypeInformation<?>> types = new ArrayList<>();
		List<String> names = new ArrayList<>();
		for (Type field : parquetFields) {
			TypeInformation<?> subType = convertField(field);
			if (subType != null) {
				types.add(subType);
				names.add(field.getName());
			}
		}

		return new RowTypeInfo(types.toArray(new TypeInformation<?>[types.size()]),
			names.toArray(new String[names.size()]));
	}

	private static TypeInformation<?> convertField(final Type fieldType) {
		TypeInformation<?> typeInfo = null;
		if (fieldType.isPrimitive()) {
			OriginalType originalType = fieldType.getOriginalType();
			PrimitiveType primitiveType = fieldType.asPrimitiveType();
			switch (primitiveType.getPrimitiveTypeName()) {
				case BINARY:
					if (originalType != null) {
						switch (originalType) {
							case DECIMAL:
								typeInfo = BasicTypeInfo.BIG_DEC_TYPE_INFO;
								break;
							case UTF8:
							case ENUM:
							case JSON:
							case BSON:
								typeInfo = BasicTypeInfo.STRING_TYPE_INFO;
								break;
							default:
								throw new UnsupportedOperationException("Unsupported original type : " + originalType.name()
									+ " for primitive type BINARY");
						}
					} else {
						typeInfo = BasicTypeInfo.STRING_TYPE_INFO;
					}
					break;
				case BOOLEAN:
					typeInfo = BasicTypeInfo.BOOLEAN_TYPE_INFO;
					break;
				case INT32:
					if (originalType != null) {
						switch (originalType) {
							case TIME_MICROS:
							case TIME_MILLIS:
								typeInfo = SqlTimeTypeInfo.TIME;
								break;
							case TIMESTAMP_MICROS:
							case TIMESTAMP_MILLIS:
								typeInfo = SqlTimeTypeInfo.TIMESTAMP;
								break;
							case DATE:
								typeInfo = SqlTimeTypeInfo.DATE;
								break;
							case UINT_8:
							case UINT_16:
							case UINT_32:
							case INT_8:
							case INT_16:
							case INT_32:
								typeInfo = BasicTypeInfo.INT_TYPE_INFO;
								break;
							default:
								throw new UnsupportedOperationException("Unsupported original type : " + originalType.name()
									+ " for primitive type INT32");
						}
					} else {
						typeInfo = BasicTypeInfo.INT_TYPE_INFO;
					}
					break;
				case INT64:
					if (originalType != null) {
						switch (originalType) {
							case TIME_MICROS:
								typeInfo = SqlTimeTypeInfo.TIME;
								break;
							case TIMESTAMP_MICROS:
							case TIMESTAMP_MILLIS:
								typeInfo = SqlTimeTypeInfo.TIMESTAMP;
								break;
							case INT_64:
							case DECIMAL:
								typeInfo = BasicTypeInfo.LONG_TYPE_INFO;
								break;
							default:
								throw new UnsupportedOperationException("Unsupported original type : " + originalType.name()
									+ " for primitive type INT64");
						}
					} else {
						typeInfo = BasicTypeInfo.LONG_TYPE_INFO;
					}
					break;
				case INT96:
					// It stores a timestamp type data, we read it as millisecond
					typeInfo = SqlTimeTypeInfo.TIMESTAMP;
					break;
				case FLOAT:
					typeInfo = BasicTypeInfo.FLOAT_TYPE_INFO;
					break;
				case DOUBLE:
					typeInfo = BasicTypeInfo.DOUBLE_TYPE_INFO;
					break;
				case FIXED_LEN_BYTE_ARRAY:
					if (originalType != null) {
						switch (originalType) {
							case DECIMAL:
								typeInfo = BasicTypeInfo.BIG_DEC_TYPE_INFO;
								break;
							default:
								throw new UnsupportedOperationException("Unsupported original type : " + originalType.name()
									+ " for primitive type FIXED_LEN_BYTE_ARRAY");
						}
					} else {
						typeInfo = BasicTypeInfo.BIG_DEC_TYPE_INFO;
					}
					break;
				default:
					throw new UnsupportedOperationException("Unsupported schema: " + fieldType);
			}
		} else {
			GroupType parquetGroupType = fieldType.asGroupType();
			OriginalType originalType = parquetGroupType.getOriginalType();
			if (originalType != null) {
				switch (originalType) {
					case LIST:
						if (parquetGroupType.getFieldCount() != 1) {
							throw new UnsupportedOperationException("Invalid list type " + parquetGroupType);
						}
						Type repeatedType = parquetGroupType.getType(0);
						if (!repeatedType.isRepetition(Type.Repetition.REPEATED)) {
							throw new UnsupportedOperationException("Invalid list type " + parquetGroupType);
						}

						if (repeatedType.isPrimitive()) {
							// Backward-compatibility element group doesn't exist also allowed
							typeInfo = BasicArrayTypeInfo.getInfoFor(
								Array.newInstance(convertField(repeatedType).getTypeClass(), 0).getClass());
						} else {
							// Backward-compatibility element group name can be any string (element/array/other)
							GroupType elementType = repeatedType.asGroupType();
							if (elementType.getFieldCount() > 1) {
								typeInfo = ObjectArrayTypeInfo.getInfoFor(convertField(elementType));
							} else {
								Type internalType = elementType.getType(0);
								if (internalType.isPrimitive()) {
									typeInfo = BasicArrayTypeInfo.getInfoFor(
										Array.newInstance(convertField(internalType).getTypeClass(),
											0).getClass());
								} else {
									typeInfo = ObjectArrayTypeInfo.getInfoFor(convertField(internalType));
								}
							}
						}
						break;

					case MAP_KEY_VALUE:
					case MAP:
						if (parquetGroupType.getFieldCount() != 1 || parquetGroupType.getType(0).isPrimitive()) {
							throw new UnsupportedOperationException("Invalid map type " + parquetGroupType);
						}

						GroupType mapKeyValType = parquetGroupType.getType(0).asGroupType();
						if (!mapKeyValType.isRepetition(Type.Repetition.REPEATED)
							|| mapKeyValType.getFieldCount() != 2) {
							throw new UnsupportedOperationException("Invalid map type " + parquetGroupType);
						}
						Type keyType = mapKeyValType.getType(0);
						if (!keyType.isPrimitive()
							|| !keyType.asPrimitiveType().getPrimitiveTypeName().equals(
								PrimitiveType.PrimitiveTypeName.BINARY)
							|| !keyType.getOriginalType().equals(OriginalType.UTF8)) {
							throw new IllegalArgumentException("Map key type must be binary (UTF8): "
								+ keyType);
						}

						Type valueType = mapKeyValType.getType(1);
						return new MapTypeInfo<>(BasicTypeInfo.STRING_TYPE_INFO, convertField(valueType));
					default:
						throw new UnsupportedOperationException("Unsupported schema: " + fieldType);
				}
			} else {
				// if no original type than it is a record
				return convertFields(parquetGroupType.getFields());
			}
		}

		return typeInfo;
	}

	private static Type convertField(String fieldName, TypeInformation<?> typeInfo,
									Type.Repetition inheritRepetition, boolean isStandard) {
		Type fieldType = null;

		Type.Repetition repetition = inheritRepetition == null ? Type.Repetition.OPTIONAL : inheritRepetition;
		if (typeInfo.isBasicType()) {
			BasicTypeInfo basicTypeInfo = (BasicTypeInfo) typeInfo;
			if (basicTypeInfo.equals(BasicTypeInfo.BIG_DEC_TYPE_INFO)) {
				fieldType = Types.primitive(PrimitiveType.PrimitiveTypeName.DOUBLE, repetition).named(fieldName);
			} else if (basicTypeInfo.equals(BasicTypeInfo.BIG_INT_TYPE_INFO)) {
				fieldType = Types.primitive(PrimitiveType.PrimitiveTypeName.INT96, repetition).named(fieldName);
			} else if (basicTypeInfo.equals(BasicTypeInfo.INT_TYPE_INFO)) {
				fieldType = Types.primitive(PrimitiveType.PrimitiveTypeName.INT32, repetition).named(fieldName);
			} else if (basicTypeInfo.equals(BasicTypeInfo.DOUBLE_TYPE_INFO)) {
				fieldType = Types.primitive(PrimitiveType.PrimitiveTypeName.DOUBLE, repetition).named(fieldName);
			} else if (basicTypeInfo.equals(BasicTypeInfo.FLOAT_TYPE_INFO)) {
				fieldType = Types.primitive(PrimitiveType.PrimitiveTypeName.FLOAT, repetition).named(fieldName);
			} else if (basicTypeInfo.equals(BasicTypeInfo.LONG_TYPE_INFO)) {
				fieldType = Types.primitive(PrimitiveType.PrimitiveTypeName.INT64, repetition).named(fieldName);
			} else if (basicTypeInfo.equals(BasicTypeInfo.SHORT_TYPE_INFO)) {
				fieldType = Types.primitive(PrimitiveType.PrimitiveTypeName.INT32, repetition).named(fieldName);
			} else if (basicTypeInfo.equals(BasicTypeInfo.BYTE_TYPE_INFO)) {
				fieldType = Types.primitive(PrimitiveType.PrimitiveTypeName.BINARY, repetition).named(fieldName);
			} else if (basicTypeInfo.equals(BasicTypeInfo.CHAR_TYPE_INFO)) {
				fieldType = Types.primitive(PrimitiveType.PrimitiveTypeName.BINARY, repetition).named(fieldName);
			} else if (basicTypeInfo.equals(BasicTypeInfo.BOOLEAN_TYPE_INFO)) {
				fieldType = Types.primitive(PrimitiveType.PrimitiveTypeName.BOOLEAN, repetition).named(fieldName);
			} else if (basicTypeInfo.equals(BasicTypeInfo.DATE_TYPE_INFO)
				|| basicTypeInfo.equals(BasicTypeInfo.STRING_TYPE_INFO)) {
				fieldType = Types.primitive(PrimitiveType.PrimitiveTypeName.BINARY, repetition)
					.as(OriginalType.UTF8)
					.named(fieldName);
			}
		} else if (typeInfo instanceof MapTypeInfo) {
			MapTypeInfo mapTypeInfo = (MapTypeInfo) typeInfo;
			fieldType = Types.optionalMap()
				.key(convertField(MAP_KEY, mapTypeInfo.getKeyTypeInfo(), Type.Repetition.REQUIRED, isStandard))
				.value(convertField(MAP_VALUE, mapTypeInfo.getValueTypeInfo(), Type.Repetition.OPTIONAL, isStandard))
				.named(fieldName);
		} else if (typeInfo instanceof ObjectArrayTypeInfo) {
			ObjectArrayTypeInfo objectArrayTypeInfo = (ObjectArrayTypeInfo) typeInfo;
			fieldType = Types.optionalGroup()
				.addField(convertField(LIST_ELEMENT, objectArrayTypeInfo.getComponentInfo(),
					Type.Repetition.REPEATED, isStandard))
				.as(OriginalType.LIST)
				.named(fieldName);
		} else if (typeInfo instanceof BasicArrayTypeInfo) {
			BasicArrayTypeInfo basicArrayType = (BasicArrayTypeInfo) typeInfo;

			if (isStandard) {

				// Add extra layer of Group according to Parquet's standard
				Type listGroup = Types.repeatedGroup().addField(
					convertField(LIST_ELEMENT, basicArrayType.getComponentInfo(),
						Type.Repetition.OPTIONAL, isStandard)).named(LIST_GROUP_NAME);

				fieldType = Types.optionalGroup()
					.addField(listGroup)
					.as(OriginalType.LIST).named(fieldName);
			} else {
				PrimitiveType primitiveTyp =
					convertField(fieldName, basicArrayType.getComponentInfo(),
						Type.Repetition.OPTIONAL, isStandard).asPrimitiveType();
				fieldType = Types.optionalGroup()
					.repeated(primitiveTyp.getPrimitiveTypeName())
					.as(primitiveTyp.getOriginalType())
					.named(LIST_ARRAY_TYPE)
					.as(OriginalType.LIST).named(fieldName);
			}
		} else {
			RowTypeInfo rowTypeInfo = (RowTypeInfo) typeInfo;
			List<Type> types = new ArrayList<>();
			String[] fieldNames = rowTypeInfo.getFieldNames();
			TypeInformation<?>[] fieldTypes = rowTypeInfo.getFieldTypes();
			for (int i = 0; i < rowTypeInfo.getArity(); i++) {
				types.add(convertField(fieldNames[i], fieldTypes[i], Type.Repetition.OPTIONAL, isStandard));
			}

			if (fieldName == null) {
				fieldType = new MessageType(MESSAGE_ROOT, types);
			} else {
				fieldType = new GroupType(repetition, fieldName, types);
			}
		}

		return fieldType;
	}

	private boolean isElementType(Type repeatedType, String parentName) {
		return (
			// can't be a synthetic layer because it would be invalid
			repeatedType.isPrimitive()
				|| repeatedType.asGroupType().getFieldCount() > 1
				|| repeatedType.asGroupType().getType(0).isRepetition(Type.Repetition.REPEATED)
				// known patterns without the synthetic layer
				|| repeatedType.getName().equals("array")
				|| repeatedType.getName().equals(parentName + "_tuple"));
	}
}
