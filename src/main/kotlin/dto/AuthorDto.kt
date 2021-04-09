package dto

import entity.Author

data class AuthorDto(
    val id : Long,
    val name : String,
    val avatar : String,
)

fun AuthorDto.dto() = Author(
    this.id,
    this.name,
    this.avatar
)