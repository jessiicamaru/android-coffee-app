package com.example.coffeeshop.redux.store

import com.example.coffeeshop.redux.data_class.AppState
import com.example.coffeeshop.redux.reducer.Reducer.Companion.appReducer
import org.reduxkotlin.createStore

class Store {
    companion object {
        val store = createStore(appReducer, AppState())
    }
}