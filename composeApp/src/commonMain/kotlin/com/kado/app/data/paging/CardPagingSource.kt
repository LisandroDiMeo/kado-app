package com.kado.app.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kado.app.data.local.dao.CardDao
import com.kado.app.data.local.entity.CardEntity

class CardPagingSource(
    private val cardDao: CardDao,
    private val deckId: Long
) : PagingSource<Int, CardEntity>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CardEntity> {
        val page = params.key ?: 0
        val pageSize = params.loadSize
        return try {
            val entities = cardDao.getByDeckIdPaged(deckId, pageSize, page * pageSize)
            LoadResult.Page(
                data = entities,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (entities.size < pageSize) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, CardEntity>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }
}
