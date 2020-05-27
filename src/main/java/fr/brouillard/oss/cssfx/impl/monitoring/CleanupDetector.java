package fr.brouillard.oss.cssfx.impl.monitoring;

/*
 * #%L
 * CSSFX
 * %%
 * Copyright (C) 2014 - 2020 CSSFX by Matthieu Brouillard
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.HashSet;

public class CleanupDetector {

    static HashSet<PhantomReferenceWithRunnable> references = new HashSet<PhantomReferenceWithRunnable>();
    static ReferenceQueue queue = new ReferenceQueue();;

    static {
        Thread cleanupDetectorThread = new Thread(() -> {
            while (true) {
                try {
                    PhantomReferenceWithRunnable r = (PhantomReferenceWithRunnable) queue.remove();
                    references.remove(r);
                    r.r.run();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }, "CSSFX-cleanup-detector");
        cleanupDetectorThread.setDaemon(true);
        cleanupDetectorThread.start();
    }

    static PhantomReferenceWithRunnable pr = null;
    public static void onCleanup(Object obj, Runnable r) {
        PhantomReferenceWithRunnable phantomref = new PhantomReferenceWithRunnable(obj,queue,r);
        references.add(phantomref);
    }

    static class PhantomReferenceWithRunnable extends PhantomReference {
        Runnable r = null;
        PhantomReferenceWithRunnable(Object ref, ReferenceQueue queue, Runnable r) {
            super(ref,queue);
            this.r = r;
        }

    }
}
