package com.cindaku.holanear.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.cindaku.holanear.BaseApp
import com.cindaku.holanear.R
import com.cindaku.holanear.activity.ChatActivity
import com.cindaku.holanear.activity.ContactChooseActivity
import com.cindaku.holanear.activity.DetailChatActivity
import com.cindaku.holanear.db.entity.ChatListWithDetail
import com.cindaku.holanear.ui.adapter.ChatListAdapter
import com.cindaku.holanear.ui.inf.OnChatListEvent
import com.cindaku.holanear.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

class ChatListFragment : Fragment(), OnChatListEvent {
    lateinit var viewModel: ChatViewModel
    lateinit var adapter: ChatListAdapter
    lateinit var list: RecyclerView;
    lateinit var fab: FloatingActionButton
    lateinit var toolbar: Toolbar
    lateinit var backImageView: ImageView
    lateinit var titleTextView: TextView
    var onSelectedContact=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode== Activity.RESULT_OK){
            it.data?.let {
                val result=it.getStringExtra("contact")
                val intent=Intent(requireContext(),DetailChatActivity::class.java)
                intent.putExtra("contact",result)
                startActivity(intent)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel=ViewModelProvider(requireActivity()).get(ChatViewModel::class.java)
        (requireActivity().application as BaseApp).appComponent.inject(viewModel)
        (requireContext() as ChatActivity).setOnChatEvent(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list=view.findViewById(R.id.chatRecyclerView)
        fab=view.findViewById(R.id.floatingActionButton)
        fab.setOnClickListener {
            val intent=Intent(requireContext(),ContactChooseActivity::class.java)
            intent.putExtra("toSend",false)
            onSelectedContact.launch(intent)
        }
        adapter= ChatListAdapter(requireContext())
        list.layoutManager=LinearLayoutManager(requireContext())
        list.adapter=adapter
        adapter.setInf(this)
        loadChatList()
    }
    companion object {
        @JvmStatic
        fun newInstance() = ChatListFragment()
    }
    fun loadChatList(){
        lifecycleScope.launch {
            viewModel.getAllChatList().collect {
                adapter.setData(it)
            }
        }
    }
    override fun onClickItem(chatMessaListWithDetail: ChatListWithDetail) {
        chatMessaListWithDetail.contact?.let {
            val intent= Intent(requireContext(), DetailChatActivity::class.java)
            intent.putExtra("contact", Gson().toJson(it))
            requireActivity().startActivity(intent)
        }
    }

    override fun onAdd(chatMessaListWithDetail: ChatListWithDetail) {
        viewModel.selected.add(chatMessaListWithDetail)
        if(viewModel.selected.size>0){
            viewModel.isSelectionMode=true
        }
        adapter.notifyDataSetChanged()
        requireActivity().invalidateOptionsMenu()
    }

    override fun onRemove(chatMessaListWithDetail: ChatListWithDetail) {
        viewModel.selected.remove(chatMessaListWithDetail)
        if(viewModel.selected.size<1){
            viewModel.isSelectionMode=false
        }
        adapter.notifyDataSetChanged()
        requireActivity().invalidateOptionsMenu()
    }

    override fun isSelected(chatMessaListWithDetail: ChatListWithDetail): Boolean {
        return viewModel.selected.indexOf(chatMessaListWithDetail)>=0
    }

    override fun isSelectionMode(): Boolean {
        return viewModel.isSelectionMode
    }
    override fun clear(){
        viewModel.isSelectionMode=false
        viewModel.selected.clear()
        adapter.notifyDataSetChanged()
        requireActivity().invalidateOptionsMenu()
    }

    override fun deleteChat() {
        viewModel.deleteChat()
        clear()
    }

    override fun toggelMuted() {
        viewModel.toggelMuted()
        clear()
    }

    override fun toggelPin() {
        viewModel.toggelPin()
        clear()
    }

    override fun getTotalSelection(): Int {
        return viewModel.selected.size
    }
}