package com.cindaku.holanear.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.cindaku.holanear.BaseApp
import com.cindaku.holanear.R
import com.cindaku.holanear.ui.adapter.CallListAdapter
import com.cindaku.holanear.ui.viewmodel.CallViewModel
import kotlinx.coroutines.launch

class CallFragment : Fragment() {
    private lateinit var viewModel: CallViewModel
    private lateinit var list: RecyclerView
    private lateinit var adapter: CallListAdapter
    private lateinit var fab: FloatingActionButton
    var onSelectedContact=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode== Activity.RESULT_OK){
            it.data?.let {

            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel= ViewModelProvider(requireActivity()).get(CallViewModel::class.java)
        (requireActivity().application as BaseApp).appComponent.inject(viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_call, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list=view.findViewById(R.id.callRecyclerView)
        fab=view.findViewById(R.id.floatingActionButton)
        list.layoutManager=LinearLayoutManager(requireContext())
        adapter= CallListAdapter(requireContext())
        list.adapter=adapter
        loadList()
    }
    fun loadList(){
        lifecycleScope.launch {
            viewModel.getAll().collect {
                adapter.setData(it)
            }
        }
    }
    companion object {
        @JvmStatic
        fun newInstance() = CallFragment()
    }
}