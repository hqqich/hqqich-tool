package io.github.hqqich.tool.kotlinext.ext

/**
 * @author hqqich
 * 描述：实现带【泛化】返回值的Boolean链式扩展
 */

/**
 * 密封类基类，作为中间类型，实现链式调用
 */
sealed class BooleanExt<out T> // 作为生产者 + 子类型关系 = 使用协变

/**
 * 密封类子类，负责执行正向逻辑
 *【需要携带数据给中间件，完成中介二次调用】
 * 普通类，支持不同泛化
 */
class TransferData<T>(val data: T) : BooleanExt<T>()

/**
 * 密封类子类，负责执行反向逻辑
 *【不需要携带数据给中间件，用Nothing完成协变语法即可，不必泛化】
 * 单例节约内存
 */
object Otherwise : BooleanExt<Nothing>()

/**
 * 调用以扩展函数[yes]开头，以密封类为中介返回，延续链式调用
 */
inline fun <T> Boolean.yes(action: () -> T) = when {
    this -> {
        // 执行正向 lambda, 携带传递 lambda 结果，完成链式调用
        TransferData(action.invoke())
    }

    else -> {
        Otherwise
    }
}

/**
 * 中介扩展函数，处理[Boolean.yes]的另外一种情况
 */
inline fun <T> BooleanExt<T>.otherwise(action: () -> T) = when (this) {
    is Otherwise -> {
        // 执行反向的 lambda
        action.invoke()
    }

    is TransferData -> {
        this.data
    }
}
