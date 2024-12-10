package vn.xdeuhug.camerax.utils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 10 / 12 / 2024
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}