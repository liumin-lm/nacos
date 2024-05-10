/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.api.naming.pojo;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ListViewTest {

    @Test
    void testToString() {
        List<String> data = new LinkedList<>();
        data.add("1");
        data.add("2");
        data.add("3");
        ListView<String> listView = new ListView<>();
        listView.setData(data);
        listView.setCount(data.size());
        String actual = listView.toString();
        assertEquals("ListView{data=[1, 2, 3], count=3}", actual);
    }

    @Test
    void testSetAndGet() {
        ListView<String> listView = new ListView<>();
        assertEquals(0, listView.getCount());
        assertNull(listView.getData());
        listView.setCount(1);
        listView.setData(Collections.singletonList("1"));
        assertEquals(1, listView.getCount());
        assertEquals(1, listView.getData().size());
    }
}