/*
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package io.crate.planner.symbol;

import org.cratedb.DataType;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

public class StringLiteral extends ValueSymbol {

    public static final SymbolFactory<StringLiteral> FACTORY = new SymbolFactory<StringLiteral>() {
        @Override
        public StringLiteral newInstance() {
            return new StringLiteral();
        }
    };
    private String value;

    public StringLiteral(String value) {
        this.value = value;
    }

    public StringLiteral() {

    }

    @Override
    public SymbolType symbolType() {
        return SymbolType.STRING_LITERAL;
    }

    @Override
    public <C, R> R accept(SymbolVisitor<C, R> visitor, C context) {
        return visitor.visitStringLiteral(this, context);
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        String value = in.readOptionalString();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeOptionalString(value);
    }

    @Override
    public DataType valueType() {
        return DataType.STRING;
    }
}
