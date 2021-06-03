/*
 * Copyright (c) 2004-2021, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.schema.transformer;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserCredentials;
import org.junit.Test;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class UserPropertyTransformerTest
{
    private static final UUID uuid = UUID.fromString( "6507f586-f154-4ec1-a25e-d7aa51de5216" );

    @Test
    public void testUserTransform()
    {
        User user = new User();
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setUuid( uuid );
        userCredentials.setUser( user );
        userCredentials.setUsername( "test" );
        userCredentials.setUserInfo( user );

        user.setUserCredentials( userCredentials );
        user.setUser( user );

        UserPropertyTransformer transformer = new UserPropertyTransformer();
        UserPropertyTransformer.UserDto userDto = (UserPropertyTransformer.UserDto) transformer.transform( user );

        // assertEquals( uuid.toString(), userDto.getId() );
        assertEquals( user.getUid(), userDto.getId() );
        assertEquals( "test", userDto.getUsername() );
    }
}