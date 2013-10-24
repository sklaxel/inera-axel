/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.shs.broker.webconsole.message;

import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.util.convert.IConverter;
import se.inera.axel.shs.broker.messagestore.MessageLogAdminService;
import se.inera.axel.shs.broker.messagestore.MessageState;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CriteriaPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public CriteriaPanel(String id, MessageLogAdminService.Filter filter) {
        super(id);

        Form criteriaForm = new Form("criteriaForm", new CompoundPropertyModel<>(filter)) {
            @Override
            protected void onSubmit() {
                CriteriaPanel.this.onSubmit();
            }
        };

        criteriaForm.add(new TextField<String>("corrId"));
        criteriaForm.add(new TextField<String>("from"));
        criteriaForm.add(new TextField<String>("to"));
        criteriaForm.add(new TextField<String>("txId"));
        criteriaForm.add(new TextField<String>("filename"));
        criteriaForm.add(new TextField<String>("product"));

        List<MessageState> stateList = Arrays.asList(
                new MessageState[]{
                        MessageState.NEW,
                        MessageState.RECEIVED,
                        MessageState.FETCHED,
                        MessageState.SENT,
                        MessageState.QUARANTINED});

        DropDownChoice stateDropDown = new DropDownChoice("state", stateList)
        {
            private static final long serialVersionUID = 1L;
            @Override
            public IConverter getConverter(Class type) {
                if (type.equals(MessageState.class)) {
                    return new IConverter() {
                        @Override
                        public Object convertToObject(String s, Locale locale) {
                            return MessageState.valueOf(s);
                        }

                        @Override
                        public String convertToString(Object o, Locale locale) {
                            return o.toString();
                        }
                    };
                }
                return null;
            }
        };
        stateDropDown.setNullValid(true);
        criteriaForm.add(stateDropDown);

        List<Boolean> boolList = Arrays.asList(
                new Boolean[]{
                        Boolean.FALSE,
                        Boolean.TRUE});

        DropDownChoice ackDropDown = new DropDownChoice("acknowledged", boolList)
        {
            private static final long serialVersionUID = 1L;
            @Override
            public IConverter getConverter(Class type) {
                if (type.equals(Boolean.class)) {
                    return new IConverter() {
                        @Override
                        public Object convertToObject(String s, Locale locale) {
                            return Boolean.valueOf(s);
                        }

                        @Override
                        public String convertToString(Object o, Locale locale) {
                            return o.toString();
                        }
                    };
                }
                return null;
            }
        };
        ackDropDown.setNullValid(true);
        criteriaForm.add(ackDropDown);

        Button searchButton = new Button("searchButton") {
            @Override
            public void onSubmit() {
                CriteriaPanel.this.onSubmit();
            }
        };
        criteriaForm.add(searchButton);
        criteriaForm.setDefaultButton(searchButton);

        add(criteriaForm);
    }

    public void onSubmit() {
    }

}
