package me.aydgn.mymusictracker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import me.aydgn.mymusictracker.model.Song

class SongAdapter(private val songList: MutableList<Song>, private val context: Context) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songTitle: TextView = itemView.findViewById(R.id.songTitle)
        val artistName: TextView = itemView.findViewById(R.id.artistName)
        val deleteIcon: ImageView = itemView.findViewById(R.id.deleteIcon)
        val favoriteIcon: ImageView = itemView.findViewById(R.id.favoriteIcon)

        fun bind(song: Song) {
            songTitle.text = song.title
            artistName.text = song.artist
            favoriteIcon.setImageResource(
                if (song.isFavorite) R.drawable.ic_favorite else R.drawable.ic_not_favorite
            )

            // Delete
            deleteIcon.setOnClickListener {
                songList.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
                Toast.makeText(context, "Song deleted!", Toast.LENGTH_SHORT).show()
            }

            // Add/Remove
            favoriteIcon.setOnClickListener {
                song.isFavorite = !song.isFavorite
                notifyItemChanged(adapterPosition)
                Toast.makeText(context, if (song.isFavorite) "Added to favorites!" else "Removed from favorites!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songList[position])
    }

    override fun getItemCount() = songList.size
}