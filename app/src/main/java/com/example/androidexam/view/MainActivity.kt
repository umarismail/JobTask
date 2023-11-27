package com.example.androidexam.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.androidexam.adapters.ImageAdapter
import com.example.androidexam.adapters.ItemsAdapter
import com.example.androidexam.common.filterItemsByCatId
import com.example.androidexam.common.filterItemsByName
import com.example.androidexam.common.setUpIndicator
import com.example.androidexam.common.updateIndicator
import com.example.androidexam.databinding.ActivityMainBinding
import com.example.androidexam.model.ItemsModel
import com.example.androidexam.modelFactory.ImageViewModelFactory
import com.example.androidexam.modelFactory.ItemsViewModelFactory
import com.example.androidexam.repository.ImageRepository
import com.example.androidexam.repository.ItemsRepository
import com.example.androidexam.viewModel.ImageViewModel
import com.example.androidexam.viewModel.ItemsViewModel
import java.lang.Double.max


class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: ImageViewModel
    private lateinit var viewModelItem: ItemsViewModel
    private  var catId: String=""
    private var itemListRv: List<ItemsModel> = emptyList()
    private var binding: ActivityMainBinding? = null
    val adapter = ItemsAdapter(emptyList())
    private var normalYPosition: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Remove title bar
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)
        //nestedScrollView = findViewById(R.id.nested_scroll_view)
        imageLiveData()
        itemLiveData()
        viewPagerControl()
        listeners()
        manageScroll()
    }

    private fun manageScroll() {
        binding!!.linearLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                normalYPosition = binding!!.linearLayout.y
                binding!!.linearLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        val listener = ViewTreeObserver.OnScrollChangedListener {
            val location = IntArray(2)
            val scrollY = binding!!.nestedScrollView.scrollY
            binding!!.linearLayout.getLocationOnScreen(location)
            val linearLayoutY = location[1]

            if (linearLayoutY <= 0) {
                // Pin the LinearLayout to the top
                binding!!.linearLayout.y = 0f
            } else {
                // Allow normal scrolling for the LinearLayout
                val newY = max(normalYPosition.toDouble(), -scrollY.toDouble())
                binding!!.linearLayout.y = newY.toFloat()
            }
        }

        binding!!.nestedScrollView.viewTreeObserver.addOnScrollChangedListener(listener)
    }

    private fun listeners() {
        binding!!.searchEditText.addTextChangedListener(object :TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val searchText = s.toString()
                val filteredList = filterItemsByName(searchText, itemListRv)
                // Update your RecyclerView adapter with the filtered list
                adapter.updateData(filteredList)
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
    }
    private fun viewPagerControl() {
        binding!!.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                catId=position.toString()
                val updatedItemList=filterItemsByCatId(position.toString(),itemListRv)
                adapter.updateData(updatedItemList)
                updateIndicator(position, binding!!.indicatorLayout,this@MainActivity)
            }
        })
    }
    private fun itemLiveData() {
        val repositoryItem = ItemsRepository(this)
        val viewModelFactoryItem = ItemsViewModelFactory(repositoryItem)
        viewModelItem = ViewModelProvider(this, viewModelFactoryItem)[ItemsViewModel::class.java]


        val layoutManager = LinearLayoutManager(this)
        viewModelItem.itemNames.observe(this) { itemsList ->
            itemListRv=itemsList
            val updatedItemList=filterItemsByCatId("0",itemListRv)
            adapter.updateData(updatedItemList)

        }
        binding!!. itemRv.layoutManager = layoutManager
        binding!!. itemRv.adapter = adapter
    }
    private fun imageLiveData() {
        val repository = ImageRepository(this)
        val viewModelFactory = ImageViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[ImageViewModel::class.java]
        viewModel.imageUrls.observe(this) { imageList ->
            binding!!.viewPager.adapter = ImageAdapter(imageList)
            // Initialize the indicator with the number of pages
            setUpIndicator(imageList.size, binding!!.indicatorLayout,this@MainActivity)

        }
    }

}