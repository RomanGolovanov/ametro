package io.github.romangolovanov.apps.ametro.ui.navigation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import io.github.romangolovanov.apps.ametro.ui.navigation.entities.INavigationItemGroup
import io.github.romangolovanov.apps.ametro.ui.navigation.entities.NavigationCheckBoxItem
import io.github.romangolovanov.apps.ametro.ui.navigation.entities.NavigationHeader
import io.github.romangolovanov.apps.ametro.ui.navigation.entities.NavigationItem
import io.github.romangolovanov.apps.ametro.ui.navigation.entities.NavigationSplitter
import io.github.romangolovanov.apps.ametro.ui.navigation.entities.NavigationSubHeader
import io.github.romangolovanov.apps.ametro.ui.navigation.entities.NavigationTextItem

class NavigationDrawerAdapter(context: Context, items: Array<NavigationItem>) : BaseAdapter() {

    private val items: MutableList<NavigationItem>
    private val inflater: LayoutInflater

    private val viewItemTypes: Map<Class<*>, Int> = mapOf(
        NavigationHeader::class.java to 0,
        NavigationTextItem::class.java to 1,
        NavigationSplitter::class.java to 2,
        NavigationSubHeader::class.java to 3,
        NavigationCheckBoxItem::class.java to 4
    )

    private val viewItemHolderFactories: Map<Class<*>, IHolderFactory> = mapOf(
        NavigationHeader::class.java to NavigationHeaderHolderFactory(),
        NavigationTextItem::class.java to NavigationTextItemHolderFactory(),
        NavigationSplitter::class.java to NavigationSplitterHolderFactory(),
        NavigationSubHeader::class.java to NavigationSubHeaderHolderFactory(),
        NavigationCheckBoxItem::class.java to NavigationCheckBoxItemHolderFactory()
    )

    init {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this.items = flattenItems(items).toMutableList()
    }

    fun setNavigationItems(items: Array<NavigationItem>) {
        this.items.clear()
        this.items.addAll(flattenItems(items))
        notifyDataSetChanged()
    }

    override fun areAllItemsEnabled(): Boolean = false

    override fun isEnabled(position: Int): Boolean = items[position].enabled

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): NavigationItem = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = false

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: IHolder
        val view: View
        if (convertView == null) {
            val factory = viewItemHolderFactories[items[position].javaClass]!!
            view = factory.createView(inflater, parent)
            holder = factory.createHolder(view)
        } else {
            view = convertView
            holder = convertView.tag as IHolder
        }
        holder.update(items[position])
        return view
    }

    override fun getItemViewType(position: Int): Int = viewItemTypes[items[position].javaClass]!!

    override fun getViewTypeCount(): Int = viewItemTypes.size

    override fun isEmpty(): Boolean = items.isEmpty()

    private fun flattenItems(items: Array<NavigationItem>): List<NavigationItem> {
        val flattenList = mutableListOf<NavigationItem>()
        for (item in items) {
            flattenList.add(item)
            if (item is INavigationItemGroup) {
                flattenList.addAll(flattenItems(item.getItems()))
            }
        }
        return flattenList
    }
}
