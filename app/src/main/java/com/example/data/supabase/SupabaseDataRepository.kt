package com.example.data.supabase

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Data model for project statistics retrieved from Supabase PostgREST.
 */
data class SupabaseProjectStat(
    val id: String,
    val title: String,
    val totalBudget: Double,
    val status: String,
    val clientId: String,
    val developerId: String?
)

/**
 * Data model for milestone and escrow metrics retrieved from Supabase PostgREST.
 */
data class SupabaseEscrowMilestoneStat(
    val id: String,
    val projectId: String,
    val title: String,
    val amount: Double,
    val commissionAmount: Double,
    val developerAmount: Double,
    val status: String
)

/**
 * Aggregated platform analytics payload from Supabase PostgREST.
 */
data class SupabasePlatformAnalytics(
    val totalVolumeInEscrow: Double = 0.0,
    val totalCommissionCollected: Double = 0.0,
    val totalDisbursedToDevs: Double = 0.0,
    val totalProjectsCount: Int = 0,
    val activeEscrowsCount: Int = 0,
    val completedMilestonesCount: Int = 0,
    val underReviewCount: Int = 0,
    val disputedCount: Int = 0,
    val projects: List<SupabaseProjectStat> = emptyList(),
    val milestones: List<SupabaseEscrowMilestoneStat> = emptyList(),
    val isLiveFromSupabase: Boolean = false,
    val lastFetchedTimestamp: Long = System.currentTimeMillis(),
    val fetchErrorMessage: String? = null
)

/**
 * Data repository layer that interacts with Supabase using PostgREST endpoints.
 */
class SupabaseDataRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "SupabaseDataRepo"
    }

    /**
     * Resolves the Supabase base URL, cleaning redundant schemes if present.
     */
    fun getSupabaseUrl(): String {
        val raw = try {
            BuildConfig.SUPABASE_URL
        } catch (_: Exception) {
            ""
        }
        return cleanSupabaseUrl(raw)
    }

    fun getSupabaseAnonKey(): String {
        return try {
            BuildConfig.SUPABASE_ANON_KEY
        } catch (_: Exception) {
            ""
        }
    }

    private fun cleanSupabaseUrl(rawUrl: String): String {
        var clean = rawUrl.trim()
        if (clean.isBlank()) return "https://dulnugmywpcfuyshtapb.supabase.co"
        while (clean.startsWith("https://https://")) {
            clean = clean.removePrefix("https://")
        }
        while (clean.startsWith("http://http://")) {
            clean = clean.removePrefix("http://")
        }
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
            clean = "https://$clean"
        }
        return clean.trimEnd('/')
    }

    /**
     * Fetches project and escrow statistics directly from Supabase via PostgREST endpoints.
     */
    suspend fun fetchPlatformStatistics(): SupabasePlatformAnalytics = withContext(Dispatchers.IO) {
        val baseUrl = getSupabaseUrl()
        val apiKey = getSupabaseAnonKey()

        if (apiKey.isBlank()) {
            return@withContext SupabasePlatformAnalytics(
                isLiveFromSupabase = false,
                fetchErrorMessage = "Supabase Anon API key not configured in .env"
            )
        }

        try {
            val projects = fetchProjects(baseUrl, apiKey)
            val milestones = fetchMilestones(baseUrl, apiKey)

            // Aggregate metrics
            val totalInEscrow = milestones.filter { it.status == "escrow_funded" || it.status == "under_review" || it.status == "disputed" }
                .sumOf { it.amount }
            val totalCommission = milestones.filter { it.status == "released" }
                .sumOf { it.commissionAmount }
            val totalDisbursed = milestones.filter { it.status == "released" }
                .sumOf { it.developerAmount }
            val activeEscrows = milestones.count { it.status == "escrow_funded" }
            val underReview = milestones.count { it.status == "under_review" }
            val disputed = milestones.count { it.status == "disputed" }
            val completed = milestones.count { it.status == "released" }

            SupabasePlatformAnalytics(
                totalVolumeInEscrow = totalInEscrow,
                totalCommissionCollected = totalCommission,
                totalDisbursedToDevs = totalDisbursed,
                totalProjectsCount = projects.size,
                activeEscrowsCount = activeEscrows,
                completedMilestonesCount = completed,
                underReviewCount = underReview,
                disputedCount = disputed,
                projects = projects,
                milestones = milestones,
                isLiveFromSupabase = true,
                lastFetchedTimestamp = System.currentTimeMillis(),
                fetchErrorMessage = null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch stats from Supabase PostgREST: ${e.message}", e)
            SupabasePlatformAnalytics(
                isLiveFromSupabase = false,
                fetchErrorMessage = e.message ?: "PostgREST connection failed"
            )
        }
    }

    private fun fetchProjects(baseUrl: String, apiKey: String): List<SupabaseProjectStat> {
        val request = Request.Builder()
            .url("$baseUrl/rest/v1/projects?select=*")
            .addHeader("apikey", apiKey)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Accept", "application/json")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.w(TAG, "Projects fetch returned ${response.code}: ${response.message}")
                return emptyList()
            }
            val body = response.body?.string() ?: return emptyList()
            val jsonArray = JSONArray(body)
            val result = mutableListOf<SupabaseProjectStat>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                result.add(
                    SupabaseProjectStat(
                        id = obj.optString("id"),
                        title = obj.optString("title", "Untitled Project"),
                        totalBudget = obj.optDouble("total_budget", 0.0),
                        status = obj.optString("status", "in_progress"),
                        clientId = obj.optString("client_id"),
                        developerId = if (obj.isNull("developer_id")) null else obj.optString("developer_id")
                    )
                )
            }
            return result
        }
    }

    private fun fetchMilestones(baseUrl: String, apiKey: String): List<SupabaseEscrowMilestoneStat> {
        val request = Request.Builder()
            .url("$baseUrl/rest/v1/milestones?select=*")
            .addHeader("apikey", apiKey)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Accept", "application/json")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.w(TAG, "Milestones fetch returned ${response.code}: ${response.message}")
                return emptyList()
            }
            val body = response.body?.string() ?: return emptyList()
            val jsonArray = JSONArray(body)
            val result = mutableListOf<SupabaseEscrowMilestoneStat>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val amount = obj.optDouble("amount", 0.0)
                val comm = obj.optDouble("commission_amount", amount * 0.10)
                val devAmt = obj.optDouble("developer_amount", amount * 0.90)
                result.add(
                    SupabaseEscrowMilestoneStat(
                        id = obj.optString("id"),
                        projectId = obj.optString("project_id"),
                        title = obj.optString("title", "Milestone"),
                        amount = amount,
                        commissionAmount = comm,
                        developerAmount = devAmt,
                        status = obj.optString("status", "unfunded")
                    )
                )
            }
            return result
        }
    }

    /**
     * Seeds or syncs sample escrow tables in Supabase via PostgREST for testing/demo purposes.
     */
    suspend fun syncLocalDataToSupabase(
        projects: List<com.example.data.model.Project>,
        milestones: List<com.example.data.model.Milestone>
    ): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = getSupabaseUrl()
        val apiKey = getSupabaseAnonKey()
        if (apiKey.isBlank()) return@withContext false

        try {
            // Upsert projects
            val projectArray = JSONArray()
            for (p in projects) {
                val json = JSONObject().apply {
                    put("id", p.id)
                    put("client_id", p.clientId)
                    put("developer_id", p.developerId)
                    put("title", p.title)
                    put("description", p.description)
                    put("total_budget", p.totalBudget)
                    put("status", p.status)
                }
                projectArray.put(json)
            }

            val projectBody = projectArray.toString().toRequestBody("application/json".toMediaType())
            val projReq = Request.Builder()
                .url("$baseUrl/rest/v1/projects")
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(projectBody)
                .build()

            client.newCall(projReq).execute().close()

            // Upsert milestones
            val milestoneArray = JSONArray()
            for (m in milestones) {
                val json = JSONObject().apply {
                    put("id", m.id)
                    put("project_id", m.projectId)
                    put("title", m.title)
                    put("amount", m.amount)
                    put("commission_amount", m.commissionAmount)
                    put("developer_amount", m.developerAmount)
                    put("status", m.status)
                }
                milestoneArray.put(json)
            }

            val milestoneBody = milestoneArray.toString().toRequestBody("application/json".toMediaType())
            val msReq = Request.Builder()
                .url("$baseUrl/rest/v1/milestones")
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(milestoneBody)
                .build()

            client.newCall(msReq).execute().close()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Sync to Supabase error: ${e.message}")
            false
        }
    }
}
