/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.ims.rcs.uce.presence.pidfparser.pidf;

import android.net.Uri;

import com.android.ims.rcs.uce.presence.pidfparser.ElementBase;
import com.android.internal.annotations.VisibleForTesting;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The "present" element is the root element of an "application/pidf+xml" object.
 */
public class Presence extends ElementBase {
    /**
     * The presence element consists the following elements:
     * 1: Any number (including 0) of <tuple> elements
     * 2: Any number (including 0) of <note> elements
     * 3: Any number of OPTIONAL extension elements from other namespaces.
     */

    /** The name of this element */
    public static final String ELEMENT_NAME = "presence";

    private static final String ATTRIBUTE_NAME_ENTITY = "entity";

    // The presence element must have an "entity" attribute.
    private String mEntity;

    // The presence element contains any number of <tuple> elements
    private final List<Tuple> mTupleList = new ArrayList<>();

    // The presence element contains any number of <note> elements;
    private final List<Note> mNoteList = new ArrayList<>();

    public Presence() {
    }

    public Presence(Uri contact) {
        initEntity(contact);
    }

    private void initEntity(Uri contact) {
        mEntity = contact.toString();
    }

    @VisibleForTesting
    public void setEntity(String entity) {
        mEntity = entity;
    }

    @Override
    protected String initNamespace() {
        return PidfConstant.NAMESPACE;
    }

    @Override
    protected String initElementName() {
        return ELEMENT_NAME;
    }

    public String getEntity() {
        return mEntity;
    }

    public void addTuple(Tuple tuple) {
        mTupleList.add(tuple);
    }

    public List<Tuple> getTupleList() {
        return Collections.unmodifiableList(mTupleList);
    }

    public void addNote(Note note) {
        mNoteList.add(note);
    }

    public List<Note> getNoteList() {
        return Collections.unmodifiableList(mNoteList);
    }

    @Override
    public void serialize(XmlSerializer serializer) throws IOException {
        String namespace = getNamespace();
        String elementName = getElementName();

        serializer.startTag(namespace, elementName);
        // entity attribute
        serializer.attribute(XmlPullParser.NO_NAMESPACE, ATTRIBUTE_NAME_ENTITY, mEntity);

        // tuple elements
        for (Tuple tuple : mTupleList) {
            tuple.serialize(serializer);
        }

        // note elements
        for (Note note : mNoteList) {
            note.serialize(serializer);
        }
        serializer.endTag(namespace, elementName);
    }

    @Override
    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        String namespace = parser.getNamespace();
        String name = parser.getName();

        if (!verifyParsingElement(namespace, name)) {
            throw new XmlPullParserException("Incorrect element: " + namespace + ", " + name);
        }

        mEntity = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, ATTRIBUTE_NAME_ENTITY);

        // Move to the next event.
        int eventType = parser.next();

        while(!(eventType == XmlPullParser.END_TAG
                && getNamespace().equals(parser.getNamespace())
                && getElementName().equals(parser.getName()))) {

            if (eventType == XmlPullParser.START_TAG) {
                String tagName = parser.getName();

                if (isTupleElement(eventType, tagName)) {
                    Tuple tuple = new Tuple();
                    tuple.parse(parser);
                    mTupleList.add(tuple);
                } else if (isNoteElement(eventType, tagName)) {
                    Note note = new Note();
                    note.parse(parser);
                    mNoteList.add(note);
                }
            }

            eventType = parser.next();

            // Leave directly if the event type is the end of the document.
            if (eventType == XmlPullParser.END_DOCUMENT) {
                return;
            }
        }
    }

    private boolean isTupleElement(int eventType, String name) {
        return (eventType == XmlPullParser.START_TAG && Tuple.ELEMENT_NAME.equals(name)) ?
                true : false;
    }

    private boolean isNoteElement(int eventType, String name) {
        return (eventType == XmlPullParser.START_TAG && Note.ELEMENT_NAME.equals(name)) ?
                true : false;
    }
}
