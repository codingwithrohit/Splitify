//package com.example.splitify.data.remote
//
//import io.github.jan.supabase.BuildConfig
//import io.github.jan.supabase.SupabaseClient
//import io.github.jan.supabase.createSupabaseClient
//import io.github.jan.supabase.gotrue.Auth
//import io.github.jan.supabase.postgrest.Postgrest
//import io.github.jan.supabase.realtime.Realtime
//
////Single instance will be used throughout the project
//object SupabaseClientProvider {
//
//    val client: SupabaseClient by lazy {
//        createSupabaseClient(
//            supabaseUrl = com.example.splitify.BuildConfig.SUPABASE_URL,
//            supabaseKey = com.example.splitify.BuildConfig.SUPABASE_KEY,
//        ){
//            install(Auth)
//            install(Postgrest)
//            install(Realtime)
//        }
//    }
//}