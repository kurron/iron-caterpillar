/*
 * Copyright (c) 2015 Ronald D. Kurr kurr@jvmguy.com
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
package org.kurron.feedback.exceptions;

import org.kurron.feedback.FeedbackContext;
import org.springframework.http.HttpStatus;

/**
 * Signals that the sent message digest does not match the calculated digest, and a 412 (precondition failed) should be
 * returned to the client.
 */
public class MismatchedDigestError extends AbstractError
{
    public MismatchedDigestError( final FeedbackContext context, final Object... arguments )
    {
        super( context, arguments );
    }

    @Override
    public HttpStatus getHttpStatus()
    {
        return HttpStatus.PRECONDITION_FAILED;
    }

    @Override
    public String getDeveloperMessage()
    {
        return "Did you forget Base64 encode your MD5 digest?";
    }
}
