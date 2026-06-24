/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021-2026 OpenInfra Foundation Europe. All rights reserved.
 * ================================================================================
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.common.utils.test;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.filters.FilterNonConcrete;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.EqualsAndHashCodeMatchRule;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.NoPublicFieldsExceptStaticFinalRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Utility class for testing POJO classes using OpenPojo validation framework.
 * Validates getter/setter methods, equals/hashCode contracts, and other POJO conventions.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PojoTester {

    // excludes classes whose simple name ends with any of these suffixes
    private static final String ENDS_WITH_PATTERN =
            ".*(Utils|Converter|Comparator|Builder|Factory|Impl)$";

    // excludes test fixtures and stubs (Dummy prefix)
    private static final String DUMMY_PATTERN = "Dummy.*";

    // excludes inner/anonymous classes (e.g. Foo$Bar, Foo$1) and empty simple names
    private static final String INNER_CLASS_PATTERN = ".*\\$.*|";

    private static final String BUILTIN_EXCLUDE_PATTERN =
            ENDS_WITH_PATTERN + "|" + DUMMY_PATTERN + "|" + INNER_CLASS_PATTERN;

    /**
     * Tests all POJOs in the specified package (non-recursive), excluding classes matching
     * the builtin pattern plus any additional exclusion patterns provided by the caller.
     *
     * @param packageName the package to scan for POJO classes (no subpackages)
     * @param additionalExclusions extra regex fragments to exclude (matched against simple name)
     */
    public static void testPojosFlat(String packageName, String... additionalExclusions) {
        validate(PojoClassFactory.getPojoClasses(packageName, new FilterNonConcrete()), additionalExclusions);
    }

    /**
     * Tests all POJOs in the specified package (recursive), excluding classes matching
     * the builtin pattern plus any additional exclusion patterns provided by the caller.
     *
     * <p>Additional exclusions are exact simple-name matches (anchored by {@code matches()}).
     * Use {@code .*Foo.*} if substring matching is needed.
     *
     * @param packageName the package to scan for POJO classes
     * @param additionalExclusions extra regex fragments to exclude (matched against simple name)
     */
    public static void testPojos(String packageName, String... additionalExclusions) {
        validate(PojoClassFactory.getPojoClassesRecursively(packageName, new FilterNonConcrete()),
                additionalExclusions);
    }

    private static void validate(List<PojoClass> pojoClasses, String... additionalExclusions) {
        var pattern = buildPattern(additionalExclusions);
        pojoClasses.removeIf(pc -> isTestClass(pc.getClazz())
                || pc.getClazz().getSimpleName().matches(pattern));
        if (pojoClasses.isEmpty()) {
            throw new IllegalArgumentException("No POJO classes found");
        }
        pojoClasses.forEach(pc -> log.info("Testing class: {}", pc.getClazz().getSimpleName()));

        var validator = ValidatorBuilder
                .create()
                .with(new SetterMustExistRule())
                .with(new GetterMustExistRule())
                .with(new EqualsAndHashCodeMatchRule())
                .with(new NoPublicFieldsExceptStaticFinalRule())
                .with(new SetterTester())
                .with(new GetterTester())
                .with(new ToStringTester())
                .build();
        validator.validate(pojoClasses);
    }

    private static boolean isTestClass(Class<?> clazz) {
        var codeSource = clazz.getProtectionDomain().getCodeSource();
        return codeSource != null && codeSource.getLocation().getPath().contains("test-classes");
    }

    private static String buildPattern(String... additionalExclusions) {
        if (ArrayUtils.isEmpty(additionalExclusions)) {
            return BUILTIN_EXCLUDE_PATTERN;
        }
        return BUILTIN_EXCLUDE_PATTERN + "|" + String.join("|", additionalExclusions);
    }
}
