package me.nluk.rozsada1.lib

import com.google.firebase.firestore.model.DocumentKey
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FieldPath
import dev.gitlive.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

sealed class FirestorePaging<T>

data class PreviousPage<T>(val state: T?,val limit: Long) : FirestorePaging<T>() {
    companion object{
        fun <T2> of(state : T2?, limit: Long) = PreviousPage(state, limit)
    }
}
data class NextPage<T>(val state: T?, val limit: Long) : FirestorePaging<T>(){
    companion object{
        fun <T2> of(state : T2?, limit: Long) = NextPage(state, limit)
    }
}

val DOCUMENT_ID = FieldPath(DocumentKey.KEY_FIELD_NAME)


fun Query.paginate(lastVisibleItem: Flow<Int>): Flow<List<DocumentSnapshot>> = flow {
    val documents = mutableListOf<DocumentSnapshot>()
    documents.addAll(
        this@paginate.limit(25).get().documents
    )
    emit(documents)
    lastVisibleItem.transform { lastVisible ->
        if (lastVisible == documents.size && documents.size > 0) {
            documents.addAll(
                Query(this@paginate.android.startAfter(documents.last()).limit(25)).get().documents
            )
            emit(documents)
        }
    }
}