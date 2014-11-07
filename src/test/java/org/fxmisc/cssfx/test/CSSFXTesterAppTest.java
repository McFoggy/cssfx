package org.fxmisc.cssfx.test;

/*
 * #%L
 * CSSFX
 * %%
 * Copyright (C) 2014 CSSFX by Matthieu Brouillard
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


import org.fxmisc.cssfx.test.ui.AbstractTestableGUITest;
import org.junit.Before;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

public class CSSFXTesterAppTest extends AbstractTestableGUITest<CSSFXTesterApp> {
    public CSSFXTesterAppTest() {
        super(CSSFXTesterApp.class);
    }
    
    @Before
    public void init() throws Exception {
        if (stage != null) {
            FXTestUtils.invokeAndWait(() -> getTestedInstance().initUI(stage), 5);
        }
    }
    
    @Test
    public void canLoadTheApplication() {
        click("#dynamicBar");
        click("#dynamicCSS");
        click("#dynamicStage");
        
        sleep(100);
    }
}
