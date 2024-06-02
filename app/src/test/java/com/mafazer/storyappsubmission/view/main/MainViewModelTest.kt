package com.mafazer.storyappsubmission.view.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.mafazer.storyappsubmission.DataDummy
import com.mafazer.storyappsubmission.MainDispatcherRule
import com.mafazer.storyappsubmission.data.UserRepository
import com.mafazer.storyappsubmission.data.local.DbListStoryItem
import com.mafazer.storyappsubmission.data.local.toDbModel
import com.mafazer.storyappsubmission.getOrAwaitValue
import com.mafazer.storyappsubmission.view.adapter.StoryListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@ExperimentalCoroutinesApi
class MainViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: UserRepository

    @Test
    fun `when Get Story Should Not Null and Return Data`() = runTest {
        val dummyStory = DataDummy.generateDummyStoryResponse().map { it.toDbModel() }
        val data: PagingData<DbListStoryItem> = PagingData.from(dummyStory)
        val expectedStory: Flow<PagingData<DbListStoryItem>> = flowOf(data)
        Mockito.`when`(storyRepository.getStoriesPaged()).thenReturn(expectedStory)

        val mainViewModel = MainViewModel(storyRepository)
        val actualStory: PagingData<DbListStoryItem> = mainViewModel.stories.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryListAdapter.getDiffCallback(),
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStory)

        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyStory.size, differ.snapshot().size)
        Assert.assertEquals(dummyStory[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest {
        val data: PagingData<DbListStoryItem> = PagingData.from(emptyList())
        val expectedStory: Flow<PagingData<DbListStoryItem>> = flowOf(data)
        Mockito.`when`(storyRepository.getStoriesPaged()).thenReturn(expectedStory)

        val mainViewModel = MainViewModel(storyRepository)
        val actualStory: PagingData<DbListStoryItem> = mainViewModel.stories.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryListAdapter.getDiffCallback(),
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStory)

        Assert.assertEquals(0, differ.snapshot().size)
    }

    val noopListUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }
}