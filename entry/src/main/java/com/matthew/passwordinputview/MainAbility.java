/*
 * Copyright (C) 2020-21 Application Library Engineering Group
 *
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
 */

package com.matthew.passwordinputview;

import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.agp.utils.TextAlignment;
import ohos.agp.window.dialog.ToastDialog;
import com.matthew.passwordinput.lib.PasswordInputView;

/**
 * Main Ability class.
 */
public class MainAbility extends Ability {

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        setUIContent(ResourceTable.Layout_ability_main);
        PasswordInputView passwordView = (PasswordInputView) findComponentById(ResourceTable.Id_passwordView);
        passwordView.setInputListener(new PasswordInputView.InputListener() {
            @Override
            public void onInputCompleted(String text) {
                ToastDialog toastDialog = new ToastDialog(getApplicationContext());
                toastDialog.setText(text);
                toastDialog.setAlignment(TextAlignment.BOTTOM);
                toastDialog.show();
            }
        });
    }
}