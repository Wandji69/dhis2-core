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

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;

import org.hisp.dhis.analytics.event.data.aggregated.sql.transform.FunctionXt;
import org.hisp.dhis.analytics.event.data.aggregated.sql.transform.model.element.inner_join.InnerJoinElement;
import org.hisp.dhis.analytics.event.data.aggregated.sql.transform.model.element.where.PredicateElement;

/**
 * @author Dusan Bernat
 */
public class SqlLatestExpressionProvider
{
    public FunctionXt<String, List<PredicateElement>> getProvider()
    {

        return sqlStatement -> {

            SqlInnerJoinElementProvider sqlInnerJoinElementProvider = new SqlInnerJoinElementProvider();

            Statement select = CCJSqlParserUtil.parse( sqlStatement );

            StringBuilder from = new StringBuilder();

            StringBuilder to = new StringBuilder();

            Optional<InnerJoinElement> innerJoinElement = sqlInnerJoinElementProvider.getProvider()
                .apply( sqlStatement ).stream().findFirst();

            if ( !innerJoinElement.isPresent() )
            {
                throw new JSQLParserException();
            }

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
                                public void visit( MinorThan expr )
                                {
                                    expr.accept( getVisitorForMinorThen( to ) );
                                }

                                @Override
                                public void visit( GreaterThanEquals expr )
                                {
                                    expr.accept( getVisitorForGreaterThenEquals( from ) );
                                }
                            } );
                        }
                    } );
                }
            } );

            return Collections.singletonList(
                new PredicateElement( innerJoinElement.get().getTableElement().getAlias() + ".latest ",
                    from + " and " + to, "between", "and" ) );
        };
    }

    private static ExpressionVisitorAdapter getVisitorForGreaterThenEquals( StringBuilder from )
    {
        return new ExpressionVisitorAdapter()
        {
            @Override
            public void visit( CastExpression expr )
            {
                expr.getLeftExpression().accept( new ExpressionVisitorAdapter()
                {
                    @Override
                    public void visit( StringValue value )
                    {
                        try
                        {
                            from.append( value );
                        }
                        catch ( DateTimeParseException ignored )
                        {
                        }
                    }
                } );
            }
        };
    }

    private static ExpressionVisitorAdapter getVisitorForMinorThen( StringBuilder to )
    {
        return new ExpressionVisitorAdapter()
        {
            @Override
            public void visit( CastExpression expr )
            {
                expr.getLeftExpression().accept( new ExpressionVisitorAdapter()
                {
                    @Override
                    public void visit( StringValue value )
                    {
                        try
                        {
                            LocalDate ld = LocalDate
                                .parse( value.toString().replace( "'", "" ) ).minusDays( 1 );
                            to.append( "'" ).append( ld ).append( "'" );
                        }
                        catch ( DateTimeParseException ignored )
                        {
                        }
                    }
                } );
            }
        };
    }
}