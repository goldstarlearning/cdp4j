/**
 * The MIT License
 * Copyright © 2017 WebFolder OÜ
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.webfolder.cdp.session;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.webfolder.cdp.command.Runtime;
import io.webfolder.cdp.exception.CdpException;
import io.webfolder.cdp.type.constant.ObjectType;
import io.webfolder.cdp.type.runtime.CallArgument;
import io.webfolder.cdp.type.runtime.CallFunctionOnResult;
import io.webfolder.cdp.type.runtime.EvaluateResult;
import io.webfolder.cdp.type.runtime.RemoteObject;

public interface JavaScript {

    default Object evaluate(String expression) {
        Runtime runtime = getThis().getCommand().getRuntime();
        EvaluateResult result = runtime.evaluate(expression, null, null,
                                                    null, null, null,
                                                    null, null, null);
        if (result == null) {
            return null;
        }
        RemoteObject remoteObject = result.getResult();
        if (remoteObject == null) {
            return null;
        }
        String objectId = result.getResult().getObjectId();
        if (objectId != null) {
            getThis().releaseObject(objectId);
        }
        Object value = remoteObject.getValue();
        getThis().logExit("evaluate", expression, value);
        return value;
    }

    default void callFunction(String name) {
        callFunction(name, void.class, Constant.EMPTY_ARGS);
    }

    default <T> T callFunction(String name, Class<T> returnType) {
        return callFunction(name, returnType, Constant.EMPTY_ARGS);
    }

    @SuppressWarnings("unchecked")
    default <T> T callFunction(String name, Class<T> returnType, Object ...arguments) {
        EvaluateResult windowResult = getThis().getCommand().getRuntime().evaluate("window");

        if (windowResult == null) {
            return null;
        }

        if ( windowResult.getExceptionDetails() != null &&
                            windowResult.getExceptionDetails().getException() != null ) {
            getThis().releaseObject(windowResult.getExceptionDetails().getException().getObjectId());
            throw new CdpException(windowResult.getExceptionDetails().getException().getDescription());
        }

        CallArgument objArgument = new CallArgument();
        objArgument.setValue(name);

        CallFunctionOnResult funcObj = getThis()
                .getCommand()
                .getRuntime()
                .callFunctionOn(windowResult.getResult().getObjectId(),
                        "function(functionName) { return functionName.split('.').reduce((o, i) => o[i], this); }",
                                                        asList(objArgument),
                                                        FALSE, FALSE,
                                                        FALSE, FALSE,
                                                        FALSE);

        if ( funcObj.getExceptionDetails() != null &&
                funcObj.getExceptionDetails().getException() != null ) {
            getThis().releaseObject(funcObj.getExceptionDetails().getException().getObjectId());
            throw new CdpException(funcObj.getExceptionDetails().getException().getDescription());
        }

        if (ObjectType.Undefined.equals(funcObj.getResult().getType())) {
            getThis().releaseObject(funcObj.getResult().getObjectId());
            throw new CdpException(format("Function [%s] is not defined", name));
        }

        StringJoiner argNames = new StringJoiner(",");

        List<CallArgument> argsFunc = new ArrayList<>();

        int i = 0;
        if ( arguments != null && arguments.length > 0) {
            for (Object argument : arguments) {
                CallArgument ca = new CallArgument();
                argsFunc.add(ca);
                ca.setValue(argument);
                argNames.add("arg" + (i + 1));
            }
        }

        CallFunctionOnResult func = getThis()
                                        .getCommand()
                                        .getRuntime()
                                        .callFunctionOn(funcObj.getResult().getObjectId(),
                                                format("function(%s) { const result = this.apply(this, Array.prototype.slice.call(arguments)); " +
                                                        "return typeof result === 'undefined' ? undefined : JSON.stringify({ result : result }); }",
                                                        argNames.toString()),
                                                argsFunc,
                                                FALSE, TRUE,
                                                FALSE, FALSE,
                                                FALSE);

        getThis().releaseObject(func.getResult().getObjectId());

        if ( func.getExceptionDetails() != null &&
                func.getExceptionDetails().getException() != null ) {
            getThis().releaseObject(func.getExceptionDetails().getException().getObjectId());
            throw new CdpException(func.getExceptionDetails().getException().getDescription());
        }

        Object value = null;
        if ( ObjectType.String.equals(func.getResult().getType()) && ! returnType.equals(void.class) ) {
            String json = valueOf(func.getResult().getValue());
            JsonObject object = getThis().getGson().fromJson(json, JsonObject.class);
            JsonElement result = object.get("result");
            value = getThis().getGson().fromJson(result, returnType);
        } else if (ObjectType.Undefined.equals(func.getResult().getType())) {
            value = void.class;
        }

        StringJoiner joiner = new StringJoiner("\", \"");
        for (Object o : arguments) {
            joiner.add(valueOf(o));
        }

        getThis().logExit("callFunction",
                        name + (arguments == null || arguments.length == 0 ? "" : "\", " + joiner.toString()),
                        valueOf(value).replace("\n", "").replace("\r", ""));

        return ! void.class.equals(value) ? (T) value : null;
    }

    public Session getThis();
}
