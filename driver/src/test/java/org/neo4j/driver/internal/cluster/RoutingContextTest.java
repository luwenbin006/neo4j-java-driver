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
package org.neo4j.driver.internal.cluster;

import org.junit.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RoutingContextTest
{
    @Test
    public void emptyContextIsNotDefined()
    {
        assertFalse( RoutingContext.EMPTY.isDefined() );
    }

    @Test
    public void emptyContextInEmptyMap()
    {
        assertTrue( RoutingContext.EMPTY.asMap().isEmpty() );
    }

    @Test
    public void uriWithoutQueryIsParsedToEmptyContext()
    {
        testEmptyRoutingContext( URI.create( "bolt+routing://localhost:7687/" ) );
    }

    @Test
    public void uriWithEmptyQueryIsParsedToEmptyContext()
    {
        testEmptyRoutingContext( URI.create( "bolt+routing://localhost:7687?" ) );
        testEmptyRoutingContext( URI.create( "bolt+routing://localhost:7687/?" ) );
    }

    @Test
    public void uriWithQueryIsParsed()
    {
        URI uri = URI.create( "bolt+routing://localhost:7687/?key1=value1&key2=value2&key3=value3" );
        RoutingContext context = new RoutingContext( uri );

        assertTrue( context.isDefined() );
        Map<String,String> expectedMap = new HashMap<>();
        expectedMap.put( "key1", "value1" );
        expectedMap.put( "key2", "value2" );
        expectedMap.put( "key3", "value3" );
        assertEquals( expectedMap, context.asMap() );
    }

    @Test
    public void throwsForInvalidUriQuery()
    {
        testIllegalUri( URI.create( "bolt+routing://localhost:7687/?justKey" ) );
    }

    @Test
    public void throwsForInvalidUriQueryKey()
    {
        testIllegalUri( URI.create( "bolt+routing://localhost:7687/?=value1&key2=value2" ) );
    }

    @Test
    public void throwsForInvalidUriQueryValue()
    {
        testIllegalUri( URI.create( "bolt+routing://localhost:7687/key1?=value1&key2=" ) );
    }

    @Test
    public void throwsForDuplicatedUriQueryParameters()
    {
        testIllegalUri( URI.create( "bolt+routing://localhost:7687/?key1=value1&key2=value2&key1=value2" ) );
    }

    @Test
    public void mapRepresentationIsUnmodifiable()
    {
        URI uri = URI.create( "bolt+routing://localhost:7687/?key1=value1" );
        RoutingContext context = new RoutingContext( uri );

        assertEquals( singletonMap( "key1", "value1" ), context.asMap() );

        try
        {
            context.asMap().put( "key2", "value2" );
            fail( "Exception expected" );
        }
        catch ( Exception e )
        {
            assertThat( e, instanceOf( UnsupportedOperationException.class ) );
        }

        assertEquals( singletonMap( "key1", "value1" ), context.asMap() );
    }

    private static void testIllegalUri( URI uri )
    {
        try
        {
            new RoutingContext( uri );
            fail( "Exception expected" );
        }
        catch ( Exception e )
        {
            assertThat( e, instanceOf( IllegalArgumentException.class ) );
        }
    }

    private static void testEmptyRoutingContext( URI uri )
    {
        RoutingContext context = new RoutingContext( uri );

        assertFalse( context.isDefined() );
        assertTrue( context.asMap().isEmpty() );
    }
}
