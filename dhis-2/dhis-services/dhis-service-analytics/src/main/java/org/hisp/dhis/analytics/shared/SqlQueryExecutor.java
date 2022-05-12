/*
 * Copyright (c) 2004-2022, University of Oslo
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
package org.hisp.dhis.analytics.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author maikel arabori
 * @see QueryExecutor
 */
@Component
public class SqlQueryExecutor implements QueryExecutor
{
    /**
     * @see QueryExecutor#execute(Query)
     *
     * @throws IllegalArgumentException if the query argument is null or the
     *         query contains an invalid statement (see
     *         {@link SqlQuery#validate()})
     */
    @Override
    public QueryResult execute( final Query query )
    {
        Assert.notNull( query, "The 'query' must not be null" );

        final SqlQuery sqlQuery = (SqlQuery) query;
        final String fullStatement = sqlQuery.fullStatement();
        // TODO: Execute JDBC fullStatement here and populated the map based on
        // the
        // results.
        final Map<Column, List<Object>> resultMap = new TreeMap<>();

        // Initialize map of columns with empty lists.
        for ( final Column column : sqlQuery.getColumns() )
        {
            resultMap.put( column, new ArrayList<>() );
        }

        // TODO: iterate the JDBC result set and add to the list of the
        // respective column.

        return new SqlQueryResult( resultMap );
    }
}
