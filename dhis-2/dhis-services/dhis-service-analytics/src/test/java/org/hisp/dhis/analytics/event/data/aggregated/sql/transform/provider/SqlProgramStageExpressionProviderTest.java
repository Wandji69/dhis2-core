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
package org.hisp.dhis.analytics.event.data.aggregated.sql.transform.provider;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.hisp.dhis.DhisTest;
import org.hisp.dhis.analytics.event.data.aggregated.sql.transform.model.element.where.PredicateElement;
import org.junit.Test;

public class SqlProgramStageExpressionProviderTest extends DhisTest
{
    private String sql;

    @Override
    public void setUpTest()
    {
        sql = "select count(DISTINCT pi) as value,'2020W17' as Weekly \n" +
            "from analytics_enrollment_uyjxktbwrnf as ax \n" +
            "where cast((select \"PFXeJV8d7ja\" \n" +
            "from analytics_event_uYjxkTbwRNf \n" +
            "where analytics_event_uYjxkTbwRNf.pi = ax.pi and \"PFXeJV8d7ja\" is not null and ps = 'LpWNjNGvCO5' \n" +
            "order by executiondate desc limit 1 ) as date) < cast( '2020-04-27' as date )\n" +
            "and cast((select \"PFXeJV8d7ja\" from analytics_event_uYjxkTbwRNf \n" +
            "where analytics_event_uYjxkTbwRNf.pi = ax.pi and \"PFXeJV8d7ja\" is not null and ps = 'LpWNjNGvCO5' \n" +
            "order by executiondate desc limit 1 ) as date) >= cast( '2020-04-20' as date )and (uidlevel1 = 'VGTTybr8UcS' ) \n"
            +
            "and (((select count(\"ovY6E8BSdto\") from analytics_event_uYjxkTbwRNf \n" +
            "where analytics_event_uYjxkTbwRNf.pi = ax.pi and \"ovY6E8BSdto\" is not null and \"ovY6E8BSdto\" = 'Positive' "
            +
            "and ps = 'dDHkBd3X8Ce') > 0)) limit 100001";
    }

    @Test
    public void verifySqlProgramStageExpressionProvider()
    {
        SqlProgramStageExpressionProvider provider = new SqlProgramStageExpressionProvider();

        List<PredicateElement> predicateElementList = provider.getProvider().apply( sql );

        assertEquals( 2, predicateElementList.size() );

        assertEquals( "=", predicateElementList.get( 0 ).getRelation() );

        assertEquals( "PFXeJV8d7ja.ps", predicateElementList.get( 0 ).getLeftExpression() );

        assertEquals( "'LpWNjNGvCO5'", predicateElementList.get( 0 ).getRightExpression() );

        assertEquals( "and", predicateElementList.get( 0 ).getLogicalOperator() );

        assertEquals( "=", predicateElementList.get( 1 ).getRelation() );

        assertEquals( "ovY6E8BSdto.ps", predicateElementList.get( 1 ).getLeftExpression() );

        assertEquals( "'dDHkBd3X8Ce'", predicateElementList.get( 1 ).getRightExpression() );

        assertEquals( "and", predicateElementList.get( 1 ).getLogicalOperator() );
    }
}