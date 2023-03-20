package com.asdzheng.openchat.net

import com.unfbx.chatgpt.OpenAiStreamClient
import com.unfbx.chatgpt.interceptor.OpenAILogger
import okhttp3.logging.HttpLoggingInterceptor


/**
 * @author zhengjb
 * @date on 2023/3/18
 */
object OpenClient {
    var streamApi: OpenAiStreamClient? = null

    fun build(key: String) {
        val httpLoggingInterceptor = HttpLoggingInterceptor(OpenAILogger());

        streamApi = OpenAiStreamClient.builder()
            .connectTimeout(30)
            .readTimeout(30)
            .writeTimeout(30)
            .apiKey(key)
//            .proxy(proxy)
            .apiHost("https://api.openai.com/")
            .build();
    }
}