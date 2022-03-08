/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exadel.etoolbox.querykit.core.models.qom.operands;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;

import javax.jcr.query.qom.NodeLocalName;

public class NodeLocalNameAdapter extends DynamicOperandAdapter {

    private final String selector;

    NodeLocalNameAdapter(NodeLocalName original) {
        super(original,"LOCAL_NAME");
        selector = original.getSelectorName();
    }

    @Override
    public Object getValue(EvaluationContext context) {
        if (!context.hasResource(selector)) {
            return false;
        }
        return context.getResource(selector).getName();
    }
}
