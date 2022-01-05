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

package org.apache.flink.formats.parquet;

import org.apache.flink.table.planner.runtime.stream.FsStreamingSinkITCaseBase;
import org.apache.flink.util.FileUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Checkpoint ITCase for {@link ParquetFileFormatFactory}. */
public class ParquetStreamingEncryptionITCase extends FsStreamingSinkITCaseBase {

    private static final String KEY_CONTENT = ":v0:XiK8QX/OJme4yjH8H5tinvtA2N7OeMeSRvBXaoDfinE=,\n";

    private static Path tmpDir;
    private static File keyFile;

    @BeforeClass
    public static void generateKey() throws IOException {
        tmpDir = Files.createTempDirectory("encryption").toAbsolutePath();
        keyFile = new File(tmpDir.toFile(), "test.key");
        keyFile.createNewFile();
        FileUtils.writeFileUtf8(keyFile, keyFile.getAbsolutePath() + KEY_CONTENT);
    }

    @AfterClass
    public static void cleanup() {
        FileUtils.deleteDirectoryQuietly(tmpDir.toFile());
    }

    @Override
    public String[] additionalProperties() {
        String keyPath = keyFile.getAbsolutePath();
        List<String> ret = new ArrayList<>();
        ret.add("'format'='parquet'");
        ret.add("'parquet.compression'='gzip'");

        // Enable encryption
        ret.add(
                "'parquet.crypto.factory.class'='com.apple.parquet.crypto.keytools.AppleCryptoFactory'");
        ret.add(
                "'parquet.encryption.kms.client.class'='com.apple.parquet.crypto.keytools.CustomerKmsBridge'");
        ret.add(String.format("'parquet.encryption.key.file'='%s'", keyPath));
        // ret.add("'parquet.encryption.key.material.store.internally'='false'");
        ret.add("'parquet.encryption.double.wrapping'='false'");

        // Configure uniform encryption
        ret.add("'parquet.uniform.encryption'='true'");
        ret.add(String.format("'parquet.encryption.footer.key'='%s'", keyPath));

        return ret.toArray(new String[0]);
    }
}
