package dto

import entity.Author
import entity.Post

data class PostDto(
    val id: Long,
    val authorId: Long,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
)

fun PostDto.dto(author: Author) = Post(
    this.id,
    author,
    this.content,
    this.published,
    this.likedByMe,
    this.likes
)
