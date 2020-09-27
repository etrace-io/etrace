/*
 * Copyright 2019 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etrace.consumer.storage.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class FileSystemManager {
    private FileSystem fs;

    private FileSystemManager() {
        try {
            Configuration configuration = new Configuration();
            configuration.addResource("hdfs-site-default.xml");
            configuration.addResource("hdfs-site.xml");

            fs = FileSystem.get(configuration);

            org.apache.hadoop.util.ShutdownHookManager.get().addShutdownHook(SuspendHadoopShutdownHook.INST,
                Integer.MAX_VALUE);

            // todo: replace with SmartLifeCycle
            Runtime.getRuntime().addShutdownHook(
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            fs.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        WakeUpHadoopShutdownHook.INST.run();
                    }
                }
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static FileSystemManager getInstance() {
        return FileSystemManagerHolder.instance;
    }

    public static FileSystem getFileSystem() {
        return getInstance().fs;
    }

    private static class FileSystemManagerHolder {
        private static FileSystemManager instance = new FileSystemManager();
    }

    static class SuspendHadoopShutdownHook implements Runnable {
        final static Object lock = new Object();
        static SuspendHadoopShutdownHook INST = new SuspendHadoopShutdownHook();

        private SuspendHadoopShutdownHook() {
        }

        @Override
        public void run() {
            synchronized (lock) {
                try {
                    System.out.println("Suspending hadoop shutdown hook");
                    lock.wait();
                    System.out.println("Resume hadoop shutdown hook");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class WakeUpHadoopShutdownHook implements Runnable {
        static WakeUpHadoopShutdownHook INST = new WakeUpHadoopShutdownHook();

        private WakeUpHadoopShutdownHook() {
        }

        @Override
        public void run() {
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (SuspendHadoopShutdownHook.lock) {
                System.out.println("Notify hadoop shutdown lock");
                SuspendHadoopShutdownHook.lock.notify();
                System.out.println("Finish notify hadoop shutdown lock.");
            }
        }
    }

}
