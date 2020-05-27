package fr.brouillard.oss.cssfx.test.misc;

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


import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Locale;

public class DisableOnMacCondition implements ExecutionCondition {
    private static final String MAC_OS = "macos";

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        final String osName = System.getProperty("os.name");
        final String cleanOsName = osName
                .replaceAll("\\s", "")
                .toLowerCase(Locale.ENGLISH);
        if(cleanOsName.contains(MAC_OS)) {
            return ConditionEvaluationResult.disabled("Test disabled on JVM running on " + osName);
        } else {
            return ConditionEvaluationResult.enabled("Test enabled, running on " + osName);
        }
    }
}