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
package org.hisp.dhis.analytics.event.data;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.analytics.event.EventQueryParams;
import org.hisp.dhis.common.Grid;
import org.hisp.dhis.common.GridHeader;
import org.hisp.dhis.common.IdScheme;
import org.hisp.dhis.common.QueryFilter;
import org.hisp.dhis.common.QueryItem;
import org.hisp.dhis.legend.Legend;
import org.hisp.dhis.option.Option;

/**
 * @author Dusan Bernat
 */
public class QueryItemHelper
{
    private static final String ITEM_NAME_SEP = ": ";

    private static final String NA = "[N/A]";

    private QueryItemHelper()
    {
    }

    /**
     * Returns an item value (legend) for OutputIdScheme (Code, Name, Id, Uid).
     *
     * @param itemValue the item value.
     * @param params the {@link EventQueryParams}.
     */
    public static String getItemOptionValue( String itemValue, EventQueryParams params )
    {
        Optional<Option> itemOption = params.getItemOptions().stream()
            .filter( option -> option.getDisplayName().equalsIgnoreCase( itemValue ) )
            .findFirst();

        return itemOption.map( option -> params.getOutputIdScheme() == IdScheme.UID ? option.getUid()
            : params.getOutputIdScheme() == IdScheme.CODE ? option.getCode()
                : params.getOutputIdScheme() == IdScheme.NAME ? option.getName()
                    : Long.toString( option.getId() ) )
            .orElse( null );
    }

    /**
     * Returns an item value (option) for OutputIdScheme (Code, Name, Id, Uid).
     *
     * @param itemValue the item value.
     * @param params the {@link EventQueryParams}.
     */
    public static String getItemLegendValue( String itemValue, EventQueryParams params )
    {
        Optional<Legend> itemLegend = params.getItemLegends().stream()
            .filter( legend -> legend.getDisplayName().equalsIgnoreCase( itemValue ) )
            .findFirst();

        return itemLegend.map( legend -> params.getOutputIdScheme() == IdScheme.UID ? legend.getUid()
            : params.getOutputIdScheme() == IdScheme.CODE ? legend.getCode()
                : params.getOutputIdScheme() == IdScheme.NAME ? legend.getName()
                    : Long.toString( legend.getId() ) )
            .orElse( null );
    }

    /**
     * Returns an item value for the given query, query item and value. Assumes
     * that data dimensions are collapsed for the given query. Returns the short
     * name of the given query item followed by the item value. If the given
     * query item has a legend set, the item value is treated as an id and
     * substituted with the matching legend name. If the given query item has an
     * option set, the item value is treated as a code and substituted with the
     * matching option name.
     *
     * @param item the {@link QueryItem}.
     * @param itemValue the item value.
     */
    public static String getCollapsedDataItemValue( QueryItem item, String itemValue )
    {
        String value = item.getItem().getDisplayShortName() + ITEM_NAME_SEP;

        Legend legend;
        Option option;

        if ( item.hasLegendSet() && (legend = item.getLegendSet().getLegendByUid( itemValue )) != null )
        {
            return value + legend.getDisplayName();
        }
        else if ( item.hasOptionSet() && (option = item.getOptionSet().getOptionByCode( itemValue )) != null )
        {
            return value + option.getDisplayName();
        }
        else
        {
            itemValue = StringUtils.defaultString( itemValue, NA );

            return value + itemValue;
        }
    }

    /**
     * Returns a list of options {@link Option}.
     *
     * Based on the given Grid and EventQueryParams, this method will return the
     * options correct list of options.
     *
     * When the Grid has no rows, it will return the options specified as
     * element "filter", ie.: Zj7UnCAulEk.K6uUAvq500H:IN:A03, where "A03" is the
     * option code.
     *
     * When the Grid has rows, this method will return only the options that are
     * part of the row object.
     *
     * @param grid the Grid instance
     * @param params the EventQueryParams
     * @return a list of options based on the Grid/EventQueryParams
     */
    public static List<Option> getItemOptions( final Grid grid, final EventQueryParams params )
    {
        final List<Option> options = new ArrayList<>();

        for ( int i = 0; i < grid.getHeaders().size(); ++i )
        {
            final GridHeader gridHeader = grid.getHeaders().get( i );

            if ( gridHeader.hasOptionSet() && isNotEmpty( grid.getRows() ) )
            {
                options.addAll( getItemOptionsThatMatchesRows( grid, i ) );
            }
            else if ( gridHeader.hasOptionSet() && isEmpty( grid.getRows() ) )
            {
                options.addAll( getItemOptionsForEmptyRows( params ) );
            }
        }

        return options.stream().distinct().collect( toList() );
    }

    /**
     * This method will extract the options (based on their codes) from the
     * element filter.
     *
     * @param params the EventQueryParams
     * @return the options for empty rows
     */
    private static List<Option> getItemOptionsForEmptyRows( final EventQueryParams params )
    {
        final List<Option> options = new ArrayList<>();

        if ( isNotEmpty( params.getItems() ) )
        {
            final List<QueryItem> items = params.getItems();

            for ( final QueryItem item : items )
            {
                final boolean hasOptions = item.getOptionSet() != null
                    && isNotEmpty( item.getOptionSet().getOptions() );

                if ( hasOptions && isNotEmpty( item.getFilters() ) )
                {
                    options.addAll( getItemOptionsForFilter( item ) );
                }
            }
        }

        return options;
    }

    /**
     * For the list of rows, in the Grid, it will return only the options that
     * are part of each row object. It picks each option, from the list of all
     * options available, that matches the current header.
     *
     * @param grid the Grid
     * @param columnIndex
     * @return the list of matching options
     */
    private static List<Option> getItemOptionsThatMatchesRows( final Grid grid, final int columnIndex )
    {
        final List<Option> options = new ArrayList<>();
        final GridHeader gridHeader = grid.getHeaders().get( columnIndex );

        options.addAll( gridHeader
            .getOptionSetObject()
            .getOptions()
            .stream()
            .filter( opt -> opt != null && grid.getRows().stream().anyMatch( r -> {
                Object o = r.get( columnIndex );
                if ( o instanceof String )
                {
                    return ((String) o).equalsIgnoreCase( opt.getCode() );
                }

                return false;
            } ) ).collect( toList() ) );

        return options;
    }

    /**
     * Returns the options specified as element "filter" (option code), ie.:
     * Zj7UnCAulEk.K6uUAvq500H:IN:A03;B01, where "A03;B01" are the options
     * codes.
     *
     * The codes are split by the token ";" and the respective Option objects
     * are returned.
     *
     * @param item the QueryItem
     * @return the list of options found in the filter
     */
    private static List<Option> getItemOptionsForFilter( final QueryItem item )
    {
        final List<Option> options = new ArrayList<>();

        for ( final Option option : item.getOptionSet().getOptions() )
        {
            for ( final QueryFilter filter : item.getFilters() )
            {
                final List<String> filterSplit = Arrays
                    .stream( trimToEmpty( filter.getFilter() ).split( ";" ) )
                    .collect( toList() );
                if ( filterSplit.contains( trimToEmpty( option.getCode() ) ) )
                {
                    options.add( option );
                }
            }
        }

        return options;
    }
}
