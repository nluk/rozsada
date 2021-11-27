package me.nluk.rozsada1.services.impl;

import android.util.Log
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Query;
import me.nluk.rozsada1.lib.FirestorePaging
import me.nluk.rozsada1.lib.NextPage
import me.nluk.rozsada1.lib.PreviousPage

abstract class FirestoreService(
    val firestore : FirebaseFirestore
) {

    abstract val COLLECTION : String

    fun nextPage(query : Query, nextPage: NextPage<*>) : com.google.firebase.firestore.Query{
        var q = query.android
            .limit(nextPage.limit)
        nextPage.state?.also {
            q = q.startAfter(it)
        }
        return q
    }

    fun previousPage(query: Query, previousPage: PreviousPage<*>) : com.google.firebase.firestore.Query{
        var q = query.android
            .limitToLast(previousPage.limit)
        previousPage.state?.also {
            q = q.endBefore(it)
        }
        return q
    }

    fun queryCollection(paging: FirestorePaging<*>,  f : (CollectionReference) -> Query) : Query{
        Log.e(this.javaClass.canonicalName, "$COLLECTION query with page: $paging")
        val initialQuery = f(firestore.collection(COLLECTION))
        val pagedQuery = when(paging){
            is PreviousPage -> previousPage(initialQuery, paging)
            is NextPage -> nextPage(initialQuery, paging)
        }
        return Query(pagedQuery)
    }




}
