/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.client.android.fragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.hawkular.client.android.R;
import org.hawkular.client.android.adapter.TokensAdapter;
import org.hawkular.client.android.backend.model.Token;
import org.hawkular.client.android.util.ColorSchemer;
import org.hawkular.client.android.util.ViewDirector;
import org.jboss.aerogear.android.store.DataManager;
import org.jboss.aerogear.android.store.generator.IdGenerator;
import org.jboss.aerogear.android.store.sql.SQLStore;
import org.jboss.aerogear.android.store.sql.SQLStoreConfiguration;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupMenu;
import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;

/**
 * Tokens fragment.
 * <p/>
 * Displays available tokens.
 */

public class TokensFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        TokensAdapter.TokenMenuListener {

    @BindView(R.id.list)
    ListView list;

    @BindView(R.id.content)
    SwipeRefreshLayout contentLayout;

    @State
    @Nullable
    ArrayList<Token> tokens;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);

        setUpState(state);

        setUpBindings();

        setUpRefreshing();

        setUpTokens();
    }

    private void setUpTokens() {

        Context context = this.getActivity();
        SQLStore<Token> store = openStore(context);
        store.openSync();

        Collection<Token> array = store.readAll();
        tokens = new ArrayList<>(array);
        list.setAdapter(new TokensAdapter(getActivity(), this, tokens));
        hideRefreshing();
        showList();
    }


    private void hideRefreshing() {
        contentLayout.setRefreshing(false);
    }

    private void showList() {
        ViewDirector.of(this).using(R.id.animator).show(R.id.content);
    }

    private SQLStore<Token> openStore(Context context) {
        DataManager.config("Store", SQLStoreConfiguration.class)
                .withContext(context)
                .withIdGenerator(new IdGenerator() {
                    @Override
                    public String generate() {
                        return UUID.randomUUID().toString();
                    }
                }).store(Token.class);
        return (SQLStore<Token>) DataManager.getStore("Store");
    }

    private void setUpState(Bundle state) {
        Icepick.restoreInstanceState(this, state);
    }

    private void setUpBindings() {
        ButterKnife.bind(this, getView());
    }

    private void setUpRefreshing() {
        contentLayout.setOnRefreshListener(this);
        contentLayout.setColorSchemeResources(ColorSchemer.getScheme());
    }

    @Override public void onRefresh() {
        setUpTokens();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        tearDownState(state);
    }

    private void tearDownState(Bundle state) {
        Icepick.saveInstanceState(this, state);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        tearDownBindings();
    }

    private void tearDownBindings() {
        //TODO: Modify it
        //ButterKnife.unbind(this);
    }

    @Override public void onTokenMenuClick(View tokenView, int tokenPosition) {
        showTokenMenu(tokenView, tokenPosition);
    }

    private void showTokenMenu(final View tokenView, final int tokenPosition) {
        PopupMenu tokenMenu = new PopupMenu(getActivity(), tokenView);

        tokenMenu.getMenuInflater().inflate(R.menu.popup_tokens, tokenMenu.getMenu());

        tokenMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Token token = getTokensAdapter().getItem(tokenPosition);

                switch (menuItem.getItemId()) {
                    case R.id.menu_delete:
                        Context context = getActivity();
                        SQLStore<Token> store = openStore(context);
                        store.openSync();
                        store.remove(token.getPersona());
                        onRefresh();
                        return true;

                    default:
                        return false;
                }
            }
        });

        tokenMenu.show();
    }


    private TokensAdapter getTokensAdapter() {
        return (TokensAdapter) list.getAdapter();
    }
}
