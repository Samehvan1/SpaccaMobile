package com.spacca.app.data

import com.spacca.app.data.cache.PersistentCookiesStorage
import com.spacca.app.data.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.cookies.cookies
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData

// The backend authenticates via session cookies (req.session.customerId).
// Ktor's HttpCookies plugin stores and sends the session cookie automatically,
// so no manual Authorization header is required.
class ApiService(
    private val client: HttpClient,
    private val cookieStorage: PersistentCookiesStorage
) {
    // ---- Auth ----
    suspend fun requestOtp(phone: String): OtpResponse =
        client.post("/api/mobile/auth/request-otp") { setBody(OtpRequest(phone)) }.body()

    suspend fun verifyOtp(phone: String, otp: String, name: String? = null): VerifyOtpResponse =
        client.post("/api/mobile/auth/verify-otp") { setBody(VerifyOtpRequest(phone, otp, name)) }.body()

    suspend fun createPin(phone: String, pin: String): CreatePinResponse =
        client.post("/api/mobile/auth/create-pin") { setBody(CreatePinRequest(phone, pin)) }.body()

    suspend fun login(phone: String, pin: String): LoginResponse =
        client.post("/api/mobile/auth/login") { setBody(LoginRequest(phone, pin)) }.body()

    suspend fun logout() {
        client.post("/api/mobile/auth/logout")
    }

    /** Clears the local cookie jar (used on logout / session expiry). */
    fun clearCookies() {
        cookieStorage.clear()
    }

    suspend fun me(): MobileCustomer? =
        client.get("/api/mobile/auth/me").body<CustomerResponse>().customer

    // ---- Profile ----
    suspend fun updateProfile(body: UpdateProfileRequest): MobileCustomer? =
        client.patch("/api/mobile/me") { setBody(body) }.body<CustomerResponse>().customer

    /** Uploads an avatar image via multipart POST and returns the updated customer. */
    suspend fun uploadAvatar(imageBytes: ByteArray, fileName: String = "avatar.jpg"): MobileCustomer? =
        client.post("/api/mobile/me/avatar") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("avatar", imageBytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=$fileName")
                        })
                    }
                )
            )
        }.body<CustomerResponse>().customer

    suspend fun changePhone(body: ChangePhoneRequest): MessageResponse =
        client.post("/api/mobile/me/change-phone") { setBody(body) }.body()

    suspend fun changePin(body: ChangePinRequest): MessageResponse =
        client.post("/api/mobile/me/change-pin") { setBody(body) }.body()

    suspend fun deactivate(body: DeactivateRequest): MessageResponse =
        client.post("/api/mobile/me/deactivate") { setBody(body) }.body()

    suspend fun deleteAccount(): MessageResponse =
        client.delete("/api/mobile/me").body()

    // ---- Points ----
    suspend fun points(): PointsResponse =
        client.get("/api/mobile/points").body()

    // ---- Discounts & Offers ----
    suspend fun availableDiscounts(): List<Discount> =
        client.get("/api/mobile/discounts").body<DiscountsResponse>().discounts

    suspend fun offers(branchId: Int? = null): List<Offer> =
        client.get("/api/mobile/offers") { if (branchId != null) url { parameters.append("branchId", branchId.toString()) } }
            .body()

    suspend fun validateDiscount(code: String): ValidateDiscountResponse =
        client.get("/api/mobile/discounts/validate/$code").body()

    // ---- Branches ----
    suspend fun branches(): List<Branch> =
        client.get("/api/mobile/branches").body<BranchesResponse>().branches

    // ---- Categories ----
    suspend fun categories(): List<DrinkCategory> =
        client.get("/api/mobile/categories").body<CategoriesResponse>().categories

    // Incremental sync: returns only categories changed since `since` (ISO),
    // plus deletedIds and serverNow for advancing the sync cursor.
    suspend fun categoriesSince(since: String): CategoriesSyncResponse =
        client.get("/api/mobile/categories") { url { parameters.append("since", since) } }
            .body<CategoriesSyncResponse>()

    // ---- Catalog: category products (real prices) ----
    suspend fun categoryProducts(categoryId: Int): List<CategoryProduct> =
        client.get("/api/mobile/categories/$categoryId/drinks").body<CategoryProductsResponse>().products

    // Incremental sync: returns only products changed since `since` (ISO),
    // plus deletedIds and serverNow for advancing the sync cursor.
    suspend fun categoryProductsSince(categoryId: Int, since: String): CategoryProductsSyncResponse =
        client.get("/api/mobile/categories/$categoryId/drinks") { url { parameters.append("since", since) } }
            .body<CategoryProductsSyncResponse>()

    // ---- Catalog: drink detail (with ingredients) ----
    suspend fun drinkDetail(drinkId: Int): DrinkDetailResponse =
        client.get("/api/mobile/drinks/$drinkId").body()

    // ---- Home (slider / featured / offers) ----
    // These endpoints are not implemented on the backend yet; callers must
    // fall back to static data when they throw.
    suspend fun homeSlider(): List<HomeSliderItem> =
        client.get("/api/mobile/home/slider").body<HomeSliderResponse>().slider

    suspend fun featuredProducts(): List<HomeProduct> =
        client.get("/api/mobile/home/featured").body<HomeProductsResponse>().products

    suspend fun offers(): List<HomeProduct> =
        client.get("/api/mobile/home/offers").body<HomeProductsResponse>().products

    // ---- Favorites ----
    suspend fun favorites(): List<Favorite> =
        client.get("/api/mobile/favorites").body<FavoritesResponse>().favorites

    suspend fun addFavorite(drinkId: Int) {
        client.post("/api/mobile/favorites") { setBody(FavoriteRequest(drinkId)) }
    }

    suspend fun removeFavorite(drinkId: Int) {
        client.delete("/api/mobile/favorites/$drinkId")
    }

    // ---- Saved drinks ----
    suspend fun savedDrinks(): List<SavedDrink> =
        client.get("/api/mobile/saved-drinks").body<SavedDrinksResponse>().savedDrinks

    suspend fun addSavedDrink(body: SavedDrinkRequest) {
        client.post("/api/mobile/saved-drinks") { setBody(body) }
    }

    suspend fun removeSavedDrink(id: Int) {
        client.delete("/api/mobile/saved-drinks/$id")
    }

    // ---- Friends ----
    suspend fun friends(): List<Friend> =
        client.get("/api/mobile/friends").body<FriendsResponse>().friends

    suspend fun addFriend(phone: String) {
        client.post("/api/mobile/friends") { setBody(FriendRequest(phone)) }
    }

    suspend fun removeFriend(id: Int) {
        client.delete("/api/mobile/friends/$id")
    }

    // ---- Orders ----
    suspend fun placeOrder(body: PlaceOrderRequest): OrderDetail? =
        client.post("/api/mobile/orders") { setBody(body) }.body<OrderResponse>().order

    suspend fun orders(): List<OrderSummary> =
        client.get("/api/mobile/orders").body<OrdersResponse>().orders

    suspend fun orderDetail(id: Int): OrderDetail? =
        client.get("/api/mobile/orders/$id").body<OrderResponse>().order

    suspend fun cancelOrder(id: Int): MessageResponse =
        client.post("/api/mobile/orders/$id/cancel").body()
}
