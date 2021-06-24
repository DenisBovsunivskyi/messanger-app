
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.telegramdiplom.R
import com.example.telegramdiplom.models.CommonModel
import com.example.telegramdiplom.ui.screeens.groups.AddContactsFragment
import com.example.telegramdiplom.ui.screeens.groups.settings.AddMembersFragment
import com.example.telegramdiplom.utilits.downloadAndSetImage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.add_contacts_item.view.*

class AddMembersAdapter( listMembers: MutableList<CommonModel>) : RecyclerView.Adapter<AddMembersAdapter.AddMembersHolder>() {

    private var listItems = mutableListOf<CommonModel>()
    private var listMembers = listMembers
    class AddMembersHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.add_contact_item_fullname
        val itemLastMessage: TextView = view.add_contact_last_message
        val itemPhoto: CircleImageView = view.addContacts_item_photo
        val itemChoice:CircleImageView = view.add_сontacts_item_choice
      }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddMembersHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.add_contacts_item, parent, false)

        val holder = AddMembersHolder(view)
        holder.itemView.setOnClickListener {
          if(listItems[holder.adapterPosition].choice){
              holder.itemChoice.visibility = View.INVISIBLE
              listItems[holder.adapterPosition].choice = false
              AddMembersFragment.listMembersToDb.remove(listItems[holder.adapterPosition])
          }else{
              holder.itemChoice.visibility = View.VISIBLE
              listItems[holder.adapterPosition].choice = true
              AddMembersFragment.listMembersToDb.add(listItems[holder.adapterPosition])
          }
        }
        return holder
    }

    override fun getItemCount(): Int = listItems.size

    override fun onBindViewHolder(holder: AddMembersHolder, position: Int) {
        holder.itemName.text = listItems[position].fullname
        holder.itemLastMessage.text = listItems[position].lastMessage
        /*if(CURRENT_UID==listItems[position].id ){
            holder.creatorId.visibility = View.VISIBLE
            holder.creatorId.text = "Создатель"
        }*/
        holder.itemPhoto.downloadAndSetImage(listItems[position].photoUrl)
    }

    fun updateListItems(item: CommonModel){
        if(item in listMembers){

        }
        else{
            listItems.add(item)
        }
        notifyItemInserted(listItems.size)
    }
}