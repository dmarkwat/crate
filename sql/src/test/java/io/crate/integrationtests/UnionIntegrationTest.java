/*
 * Licensed to Crate under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.  Crate licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial
 * agreement.
 */

package io.crate.integrationtests;

import io.crate.testing.TestingHelpers;
import io.crate.testing.UseJdbc;
import org.elasticsearch.test.ESIntegTestCase;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

@ESIntegTestCase.ClusterScope(minNumDataNodes = 2)
@UseJdbc(0)
public class UnionIntegrationTest extends SQLTransportIntegrationTest {

    @Test
    public void testUnionAll2Tables() {
        createColorsAndSizes();
        execute("select color from colors " +
                "union all " +
                "select size from sizes");
        assertThat(Arrays.asList(response.rows()), containsInAnyOrder(new Object[]{"red"},
                                                                      new Object[]{"blue"},
                                                                      new Object[]{"green"},
                                                                      new Object[]{"small"},
                                                                      new Object[]{"large"}));
    }

    @Test
    public void testUnionAll3Tables() {
        createColorsAndSizes();
        execute("select size from sizes " +
                "union all " +
                "select color from colors " +
                "union all " +
                "select size from sizes");
        assertThat(Arrays.asList(response.rows()), containsInAnyOrder(new Object[]{"small"},
                                                                      new Object[]{"large"},
                                                                      new Object[]{"red"},
                                                                      new Object[]{"blue"},
                                                                      new Object[]{"green"},
                                                                      new Object[]{"small"},
                                                                      new Object[]{"large"}));
    }

    @Test
    public void testUnionAllWithOrderBy() {
        createColorsAndSizes();
        execute("select color from colors " +
                "union all " +
                "select size from sizes " +
                "order by 1");
        assertThat(TestingHelpers.printedTable(response.rows()), is("blue\n" +
                                                                    "green\n" +
                                                                    "large\n" +
                                                                    "red\n" +
                                                                    "small\n"));
    }

    @Test
    public void testUnionAllWithPrimaryKeys() {
        createColorsAndSizes();
        execute("select color from colors where id = 1 " +
                "union all " +
                "select size from sizes where id = 1 " +
                "order by 1");
        assertThat(TestingHelpers.printedTable(response.rows()), is("red\nsmall\n"));
    }

    @Test
    public void testUnionAllWithPartitionedTables() {
        createColorsAndSizesPartitioned();
        execute("select color from colors where id = 1 " +
                "union all " +
                "select size from sizes where size = 'small' " +
                "order by 1");
        assertThat(TestingHelpers.printedTable(response.rows()), is("red\nsmall\n"));
    }

    @Test
    public void testUnionAllWithGlobalAggregation() {
        createColorsAndSizes();
        execute("select count(*) from colors " +
                "union all " +
                "select count(*) from sizes " +
                "order by 1");
        assertThat(TestingHelpers.printedTable(response.rows()), is("2\n3\n"));
    }

    @Test
    public void testUnionAllWithGroupBy() {
        createColorsAndSizes();
        execute("select count(id), color from colors group by color " +
                "union all " +
                "select count(id), size from sizes group by size " +
                "order by 2");
        assertThat(TestingHelpers.printedTable(response.rows()), is("red\nsmall\n"));
    }

    @Test
    public void testUnionAllWithSubSelect() {
        createColorsAndSizes();
        execute("select * from (select color from colors order by id limit 2) a " +
                "union all " +
                "select * from (select size from sizes order by size limit 1) b " +
                "order by 1 " +
                "limit 10 offset 2");
        assertThat(TestingHelpers.printedTable(response.rows()), is("small\n"));
    }

    @Test
    public void testUnionAllWithJoin() throws Exception {
        createColorsAndSizes();
        execute("select colors.color from sizes, colors " +
                "union all " +
                "select size from sizes");
        assertThat(Arrays.asList(response.rows()), containsInAnyOrder(new Object[]{"small"},
                                                                      new Object[]{"large"},
                                                                      new Object[]{"red"},
                                                                      new Object[]{"blue"},
                                                                      new Object[]{"green"},
                                                                      new Object[]{"red"},
                                                                      new Object[]{"blue"},
                                                                      new Object[]{"green"}));
    }

    private void createColorsAndSizes() {
        execute("create table colors (id integer primary key, color string)");
        execute("create table sizes (id integer primary key, size string)");
        ensureYellow();

        execute("insert into colors (id, color) values (?, ?)", new Object[][]{
            new Object[]{1, "red"},
            new Object[]{2, "blue"},
            new Object[]{3, "green"}
        });
        execute("insert into sizes (id, size) values (?, ?)", new Object[][]{
            new Object[]{1, "small"},
            new Object[]{2, "large"},
        });
        execute("refresh table colors, sizes");
    }

    private void createColorsAndSizesPartitioned() {
        execute("create table colors (id integer primary key, color string) partitioned by (id)");
        execute("create table sizes (id integer primary key, size string primary key) partitioned by (size)");
        ensureYellow();

        execute("insert into colors (id, color) values (?, ?)", new Object[][]{
            new Object[]{1, "red"},
            new Object[]{2, "blue"},
            new Object[]{3, "green"}
        });
        execute("insert into sizes (id, size) values (?, ?)", new Object[][]{
            new Object[]{1, "small"},
            new Object[]{2, "large"},
        });
        execute("refresh table colors, sizes");
    }
}
