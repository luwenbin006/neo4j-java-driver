/*
 * Copyright (c) 2002-2018 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
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
package org.neo4j.driver.internal.async.inbound;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Test;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.neo4j.driver.v1.util.TestUtil.assertByteBufEquals;

public class MessageDecoderTest
{
    private final EmbeddedChannel channel = new EmbeddedChannel( new MessageDecoder() );

    @After
    public void tearDown()
    {
        channel.finishAndReleaseAll();
    }

    @Test
    public void shouldDecodeMessageWithSingleChunk()
    {
        assertFalse( channel.writeInbound( wrappedBuffer( new byte[]{1, 2, 3, 4, 5} ) ) );
        assertTrue( channel.writeInbound( wrappedBuffer( new byte[0] ) ) );
        assertTrue( channel.finish() );

        assertEquals( 1, channel.inboundMessages().size() );
        assertByteBufEquals( wrappedBuffer( new byte[]{1, 2, 3, 4, 5} ), channel.readInbound() );
    }

    @Test
    public void shouldDecodeMessageWithMultipleChunks()
    {
        assertFalse( channel.writeInbound( wrappedBuffer( new byte[]{1, 2, 3} ) ) );
        assertFalse( channel.writeInbound( wrappedBuffer( new byte[]{4, 5} ) ) );
        assertFalse( channel.writeInbound( wrappedBuffer( new byte[]{6, 7, 8} ) ) );
        assertTrue( channel.writeInbound( wrappedBuffer( new byte[0] ) ) );
        assertTrue( channel.finish() );

        assertEquals( 1, channel.inboundMessages().size() );
        assertByteBufEquals( wrappedBuffer( new byte[]{1, 2, 3, 4, 5, 6, 7, 8} ), channel.readInbound() );
    }

    @Test
    public void shouldDecodeMultipleConsecutiveMessages()
    {
        channel.writeInbound( wrappedBuffer( new byte[]{1, 2, 3} ) );
        channel.writeInbound( wrappedBuffer( new byte[0] ) );

        channel.writeInbound( wrappedBuffer( new byte[]{4, 5} ) );
        channel.writeInbound( wrappedBuffer( new byte[]{6} ) );
        channel.writeInbound( wrappedBuffer( new byte[0] ) );

        channel.writeInbound( wrappedBuffer( new byte[]{7, 8} ) );
        channel.writeInbound( wrappedBuffer( new byte[]{9, 10} ) );
        channel.writeInbound( wrappedBuffer( new byte[0] ) );

        assertEquals( 3, channel.inboundMessages().size() );
        assertByteBufEquals( wrappedBuffer( new byte[]{1, 2, 3} ), channel.readInbound() );
        assertByteBufEquals( wrappedBuffer( new byte[]{4, 5, 6} ), channel.readInbound() );
        assertByteBufEquals( wrappedBuffer( new byte[]{7, 8, 9, 10} ), channel.readInbound() );
    }
}
