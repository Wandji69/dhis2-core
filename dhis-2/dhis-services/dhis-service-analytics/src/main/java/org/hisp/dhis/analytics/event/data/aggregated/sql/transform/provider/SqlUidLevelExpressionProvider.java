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

import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;

import org.hisp.dhis.analytics.event.data.aggregated.sql.transform.FunctionXt;
import org.hisp.dhis.analytics.event.data.aggregated.sql.transform.model.element.where.PredicateElement;

/**
 * @author Dusan Bernat
 */
public class SqlUidLevelExpressionProvider
{
    public FunctionXt<String, PredicateElement> getProvider()
    {
        return sqlStatement -> {

            StringBuilder sbLeft = new StringBuilder();
            StringBuilder sbRight = new StringBuilder();
            StringBuilder sbExpression = new StringBuilder();

            Statement select = CCJSqlParserUtil.parse( sqlStatement );

            select.accept( new StatementVisitorAdapter()
            {
                @Override
                public void visit( Select select )
                {
                    select.getSelectBody().accept( new SelectVisitorAdapter()
                    {
                        @Override
                        public void visit( PlainSelect plainSelect )
                        {
                            plainSelect.getWhere().accept( new ExpressionVisitorAdapter()
                            {
                                @Override
                                public void visit( EqualsTo expr )
                                {
                                    if ( expr.getLeftExpression().toString().contains( "uidlevel" ) )
                                    {
                                        sbLeft.append( "ax." ).append( expr.getLeftExpression() );

                                        sbRight.append( expr.getRightExpression() );

                                        sbExpression.append( "=" );
                                    }
                                }
                            } );
                        }
                    } );
                }
            } );

            return new PredicateElement( sbLeft.toString(), sbRight.toString(), sbExpression.toString(), "" );

        };
    }
}