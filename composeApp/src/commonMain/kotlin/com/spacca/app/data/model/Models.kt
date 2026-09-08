package com.spacca.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---- Auth ----
@Serializable
data class OtpRequest(
    @SerialName("phone") val phone: String
)

@Serializable
data class OtpResponse(
    @SerialName("success") val success: Boolean? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("devOtp") val devOtp: String? = null,
    @SerialName("expiresIn") val expiresIn: Int? = null,
    @SerialName("hasPin") val hasPin: Boolean? = null
)

@Serializable
data class VerifyOtpRequest(
    @SerialName("phone") val phone: String,
    @SerialName("otp") val otp: String,
    @SerialName("name") val name: String? = null
)

@Serializable
data class CreatePinRequest(
    @SerialName("phone") val phone: String,
    @SerialName("pin") val pin: String
)

@Serializable
data class LoginRequest(
    @SerialName("phone") val phone: String,
    @SerialName("pin") val pin: String
)

// verify-otp response: { customer, hasPin }
@Serializable
data class VerifyOtpResponse(
    @SerialName("customer") val customer: MobileCustomer? = null,
    @SerialName("hasPin") val hasPin: Boolean? = null
)

// create-pin response: { success, customer }
@Serializable
data class CreatePinResponse(
    @SerialName("success") val success: Boolean? = null,
    @SerialName("customer") val customer: MobileCustomer? = null
)

// login response: { customer }
@Serializable
data class LoginResponse(
    @SerialName("customer") val customer: MobileCustomer? = null
)

// ---- Customer ----
@Serializable
data class MobileCustomer(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("birthdate") val birthdate: String? = null,
    @SerialName("gender") val gender: String? = null,
    @SerialName("avatarUrl") val avatarUrl: String? = null,
    @SerialName("preferredBranchId") val preferredBranchId: Int? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("city") val city: String? = null,
    @SerialName("loyaltyTier") val loyaltyTier: String? = null,
    @SerialName("points") val points: Int? = null,
    @SerialName("discountId") val discountId: Int? = null,
    @SerialName("createdAt") val createdAt: String? = null
)

// me response: { customer }
@Serializable
data class CustomerResponse(
    @SerialName("customer") val customer: MobileCustomer? = null
)

@Serializable
data class UpdateProfileRequest(
    @SerialName("name") val name: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("birthdate") val birthdate: String? = null,
    @SerialName("gender") val gender: String? = null,
    @SerialName("avatarUrl") val avatarUrl: String? = null,
    @SerialName("preferredBranchId") val preferredBranchId: Int? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("city") val city: String? = null
)

@Serializable
data class ChangePhoneRequest(
    @SerialName("newPhone") val newPhone: String,
    @SerialName("otp") val otp: String
)

@Serializable
data class ChangePinRequest(
    @SerialName("currentPin") val currentPin: String,
    @SerialName("newPin") val newPin: String
)

@Serializable
data class DeactivateRequest(
    @SerialName("pin") val pin: String
)

// ---- Branches ----
@Serializable
data class Branch(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("city") val city: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("openTime") val openTime: String? = null,
    @SerialName("closeTime") val closeTime: String? = null
)

@Serializable
data class BranchesResponse(
    @SerialName("branches") val branches: List<Branch> = emptyList()
)

// ---- Categories ----
@Serializable
data class DrinkCategory(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String? = null,
    @SerialName("image") val image: String? = null,
    @SerialName("sortOrder") val sortOrder: Int? = null
)

@Serializable
data class CategoriesResponse(
    @SerialName("categories") val categories: List<DrinkCategory> = emptyList()
)

// Incremental sync response for categories: only changed rows + sync cursor.
@Serializable
data class CategoriesSyncResponse(
    @SerialName("categories") val categories: List<DrinkCategory> = emptyList(),
    @SerialName("deletedIds") val deletedIds: List<Int> = emptyList(),
    @SerialName("serverNow") val serverNow: String? = null
)

// ---- Home (slider / featured / offers) ----
@Serializable
data class HomeSliderItem(
    @SerialName("id") val id: Int,
    @SerialName("image") val image: String? = null,
    @SerialName("title") val title: String? = null
)

@Serializable
data class HomeSliderResponse(
    @SerialName("slider") val slider: List<HomeSliderItem> = emptyList()
)

@Serializable
data class HomeProduct(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String? = null,
    @SerialName("image") val image: String? = null,
    @SerialName("price") val price: String? = null,
    @SerialName("originalPrice") val originalPrice: String? = null,
    @SerialName("isFeatured") val isFeatured: Boolean? = null,
    @SerialName("onSale") val onSale: Boolean? = null
)

@Serializable
data class HomeProductsResponse(
    @SerialName("products") val products: List<HomeProduct> = emptyList()
)

// ---- Catalog: Category products (real prices) ----
@Serializable
data class CategoryProduct(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("imageUrl") val imageUrl: String? = null,
    @SerialName("isCustomizable") val isCustomizable: Boolean? = null,
    @SerialName("price") val price: Double? = null,
    @SerialName("sortOrder") val sortOrder: Int? = null
)

@Serializable
data class CategoryProductsResponse(
    @SerialName("products") val products: List<CategoryProduct> = emptyList()
)

// Incremental sync response for category products: only changed rows + cursor.
@Serializable
data class CategoryProductsSyncResponse(
    @SerialName("products") val products: List<CategoryProduct> = emptyList(),
    @SerialName("deletedIds") val deletedIds: List<Int> = emptyList(),
    @SerialName("serverNow") val serverNow: String? = null
)

// ---- Catalog: Drink detail (with ingredients) ----
@Serializable
data class DrinkIngredient(
    @SerialName("slotLabel") val slotLabel: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("optionLabel") val optionLabel: String? = null,
    @SerialName("volumeLabel") val volumeLabel: String? = null,
    @SerialName("isRequired") val isRequired: Boolean? = null,
    @SerialName("customerSortOrder") val customerSortOrder: Int? = null
)

@Serializable
data class DrinkDetail(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("category") val category: String? = null,
    @SerialName("categoryId") val categoryId: Int? = null,
    @SerialName("imageUrl") val imageUrl: String? = null,
    @SerialName("isCustomizable") val isCustomizable: Boolean? = null,
    @SerialName("price") val price: Double? = null,
    @SerialName("cupSizeMl") val cupSizeMl: Int? = null
)

@Serializable
data class DrinkDetailResponse(
    @SerialName("drink") val drink: DrinkDetail? = null,
    @SerialName("ingredients") val ingredients: List<DrinkIngredient> = emptyList(),
    @SerialName("slots") val slots: List<DrinkSlot> = emptyList()
)

// ---- Customization recipe (slots -> options / type options + volumes) ----
@Serializable
data class DrinkSlotVolume(
    @SerialName("typeVolumeId") val typeVolumeId: Int? = null,
    @SerialName("volumeName") val volumeName: String? = null,
    @SerialName("extraCost") val extraCost: Double? = null,
    @SerialName("isDefault") val isDefault: Boolean? = null,
    @SerialName("isAvailable") val isAvailable: Boolean? = null,
    @SerialName("processedQty") val processedQty: Float? = null,
    @SerialName("producedQty") val producedQty: Float? = null
)

@Serializable
data class DrinkSlotTypeOption(
    @SerialName("typeOptionId") val typeOptionId: Int? = null,
    @SerialName("ingredientTypeId") val ingredientTypeId: Int? = null,
    @SerialName("inventoryIngredientId") val inventoryIngredientId: Int? = null,
    @SerialName("typeName") val typeName: String? = null,
    @SerialName("extraCost") val extraCost: Double? = null,
    @SerialName("isDefault") val isDefault: Boolean? = null,
    @SerialName("processedQty") val processedQty: Float? = null,
    @SerialName("producedQty") val producedQty: Float? = null,
    @SerialName("volumes") val volumes: List<DrinkSlotVolume> = emptyList()
)

@Serializable
data class DrinkSlotOption(
    @SerialName("optionId") val optionId: Int? = null,
    @SerialName("label") val label: String? = null,
    @SerialName("linkedIngredientId") val linkedIngredientId: Int? = null,
    @SerialName("extraCost") val extraCost: Double? = null,
    @SerialName("isDefault") val isDefault: Boolean? = null,
    @SerialName("isAvailable") val isAvailable: Boolean? = null,
    @SerialName("processedQty") val processedQty: Float? = null,
    @SerialName("producedQty") val producedQty: Float? = null
)

@Serializable
data class DrinkSlot(
    @SerialName("slotId") val slotId: Int? = null,
    @SerialName("slotLabel") val slotLabel: String? = null,
    @SerialName("isRequired") val isRequired: Boolean? = null,
    @SerialName("slotStyle") val slotStyle: String? = null,
    @SerialName("customerSortOrder") val customerSortOrder: Int? = null,
    @SerialName("isDynamic") val isDynamic: Boolean? = null,
    @SerialName("affectsCupSize") val affectsCupSize: Boolean? = null,
    @SerialName("ingredientId") val ingredientId: Int? = null,
    @SerialName("options") val options: List<DrinkSlotOption> = emptyList(),
    @SerialName("typeOptions") val typeOptions: List<DrinkSlotTypeOption> = emptyList()
)

// A single customization selection sent to the backend when placing an order.
// Mirrors the POS/KIOSK selection structure:
//   typed slot  -> { slotId, ingredientTypeId, typeVolumeId }
//   legacy slot -> { ingredientId, optionId }
@Serializable
data class DrinkSelection(
    @SerialName("slotId") val slotId: Int? = null,
    @SerialName("optionId") val optionId: Int? = null,
    @SerialName("typeVolumeId") val typeVolumeId: Int? = null,
    @SerialName("ingredientTypeId") val ingredientTypeId: Int? = null,
    @SerialName("ingredientId") val ingredientId: Int? = null
)

// ---- Favorites ----
@Serializable
data class FavoriteDrink(
    @SerialName("id") val id: Int? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("category") val category: String? = null,
    @SerialName("basePrice") val basePrice: Double? = null,
    @SerialName("imageUrl") val imageUrl: String? = null,
    @SerialName("isCustomizable") val isCustomizable: Boolean? = null
)

@Serializable
data class Favorite(
    @SerialName("id") val id: Int,
    @SerialName("drinkId") val drinkId: Int,
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("drink") val drink: FavoriteDrink? = null
)

@Serializable
data class FavoritesResponse(
    @SerialName("favorites") val favorites: List<Favorite> = emptyList()
)

@Serializable
data class FavoriteRequest(
    @SerialName("drinkId") val drinkId: Int
)

// ---- Saved drinks ----
@Serializable
data class SavedDrinkDrink(
    @SerialName("id") val id: Int? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("imageUrl") val imageUrl: String? = null,
    @SerialName("basePrice") val basePrice: Double? = null
)

@Serializable
data class SavedDrink(
    @SerialName("id") val id: Int,
    @SerialName("drinkId") val drinkId: Int,
    @SerialName("name") val name: String? = null,
    @SerialName("selections") val selections: String? = null,
    @SerialName("quantity") val quantity: Int? = null,
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("drink") val drink: SavedDrinkDrink? = null
)

@Serializable
data class SavedDrinksResponse(
    @SerialName("savedDrinks") val savedDrinks: List<SavedDrink> = emptyList()
)

@Serializable
data class SavedDrinkRequest(
    @SerialName("drinkId") val drinkId: Int,
    @SerialName("name") val name: String? = null,
    @SerialName("selections") val selections: String? = null,
    @SerialName("quantity") val quantity: Int? = null
)

// ---- Friends ----
@Serializable
data class Friend(
    @SerialName("id") val id: Int,
    @SerialName("friendName") val friendName: String? = null,
    @SerialName("friendPhone") val friendPhone: String? = null,
    @SerialName("createdAt") val createdAt: String? = null
)

@Serializable
data class FriendsResponse(
    @SerialName("friends") val friends: List<Friend> = emptyList()
)

@Serializable
data class FriendRequest(
    @SerialName("phone") val phone: String
)

// ---- Orders ----
@Serializable
data class OrderItem(
    @SerialName("drinkId") val drinkId: Int,
    @SerialName("quantity") val quantity: Int,
    @SerialName("selections") val selections: List<DrinkSelection>? = null,
    @SerialName("specialNotes") val specialNotes: String? = null
)

@Serializable
data class PlaceOrderRequest(
    @SerialName("branchId") val branchId: Int,
    @SerialName("items") val items: List<OrderItem>,
    @SerialName("paymentMethod") val paymentMethod: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("discountCode") val discountCode: String? = null
)

@Serializable
data class OrderSummary(
    @SerialName("id") val id: Int,
    @SerialName("orderNumber") val orderNumber: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("total") val total: Double? = null,
    @SerialName("subtotal") val subtotal: Double? = null,
    @SerialName("discount") val discount: Double? = null,
    @SerialName("paymentMethod") val paymentMethod: String? = null,
    @SerialName("source") val source: String? = null,
    @SerialName("branchId") val branchId: Int? = null,
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("branchName") val branchName: String? = null
)

@Serializable
data class OrdersResponse(
    @SerialName("orders") val orders: List<OrderSummary> = emptyList()
)

@Serializable
data class OrderCustomization(
    @SerialName("id") val id: Int? = null,
    @SerialName("ingredientId") val ingredientId: Int? = null,
    @SerialName("optionId") val optionId: Int? = null,
    @SerialName("typeVolumeId") val typeVolumeId: Int? = null,
    @SerialName("consumedQty") val consumedQty: Double? = null,
    @SerialName("producedQty") val producedQty: Double? = null,
    @SerialName("addedCost") val addedCost: Double? = null,
    @SerialName("slotLabel") val slotLabel: String? = null,
    @SerialName("optionLabel") val optionLabel: String? = null
)

@Serializable
data class OrderItemDetail(
    @SerialName("id") val id: Int? = null,
    @SerialName("drinkId") val drinkId: Int? = null,
    @SerialName("drinkName") val drinkName: String? = null,
    @SerialName("kitchenStation") val kitchenStation: String? = null,
    @SerialName("quantity") val quantity: Int? = null,
    @SerialName("unitPrice") val unitPrice: Double? = null,
    @SerialName("lineTotal") val lineTotal: Double? = null,
    @SerialName("specialNotes") val specialNotes: String? = null,
    @SerialName("customizations") val customizations: List<OrderCustomization>? = null
)

@Serializable
data class OrderPayment(
    @SerialName("id") val id: Int? = null,
    @SerialName("method") val method: String? = null,
    @SerialName("amount") val amount: Double? = null,
    @SerialName("status") val status: String? = null
)

@Serializable
data class OrderDetail(
    @SerialName("id") val id: Int,
    @SerialName("orderNumber") val orderNumber: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("total") val total: Double? = null,
    @SerialName("subtotal") val subtotal: Double? = null,
    @SerialName("discount") val discount: Double? = null,
    @SerialName("paymentMethod") val paymentMethod: String? = null,
    @SerialName("specialRequest") val specialRequest: String? = null,
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("branchName") val branchName: String? = null,
    @SerialName("items") val items: List<OrderItemDetail>? = null,
    @SerialName("payments") val payments: List<OrderPayment>? = null
)

@Serializable
data class OrderResponse(
    @SerialName("order") val order: OrderDetail? = null
)

// ---- Points ----
@Serializable
data class PointsResponse(
    @SerialName("points") val points: Int? = null,
    @SerialName("totalSpent") val totalSpent: String? = null,
    @SerialName("visitCount") val visitCount: Int? = null
)

// ---- Discounts & Offers ----
@Serializable
data class Discount(
    @SerialName("id") val id: Int,
    @SerialName("code") val code: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("value") val value: Double? = null,
    @SerialName("isTaxable") val isTaxable: Boolean? = null,
    @SerialName("reason") val reason: String? = null
)

@Serializable
data class DiscountsResponse(
    @SerialName("discounts") val discounts: List<Discount> = emptyList()
)

@Serializable
data class Offer(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String? = null,
    @SerialName("buyAmount") val buyAmount: Int? = null,
    @SerialName("freeAmount") val freeAmount: Int? = null,
    @SerialName("promoLabel") val promoLabel: String? = null,
    @SerialName("applicableDrinkIds") val applicableDrinkIds: List<Int> = emptyList(),
    @SerialName("rewardDrinkIds") val rewardDrinkIds: List<Int> = emptyList(),
    @SerialName("excludedDrinkIds") val excludedDrinkIds: List<Int> = emptyList()
)

@Serializable
data class ValidateDiscountResponse(
    @SerialName("valid") val valid: Boolean = false,
    @SerialName("discount") val discount: Discount? = null,
    @SerialName("error") val error: String? = null
)

// ---- Generic ----
@Serializable
data class MessageResponse(
    @SerialName("message") val message: String? = null,
    @SerialName("success") val success: Boolean? = null
)

// ---- Nutrition ----
@Serializable
data class NutritionFacts(
    @SerialName("calories") val calories: Double? = null,
    @SerialName("protein") val protein: Double? = null,
    @SerialName("totalCarbs") val totalCarbs: Double? = null,
    @SerialName("dietaryFiber") val dietaryFiber: Double? = null,
    @SerialName("totalSugars") val totalSugars: Double? = null,
    @SerialName("addedSugars") val addedSugars: Double? = null,
    @SerialName("totalFat") val totalFat: Double? = null,
    @SerialName("saturatedFat") val saturatedFat: Double? = null,
    @SerialName("transFat") val transFat: Double? = null,
    @SerialName("cholesterol") val cholesterol: Double? = null,
    @SerialName("sodium") val sodium: Double? = null,
    @SerialName("caffeine") val caffeine: Double? = null,
    @SerialName("allergens") val allergens: List<String> = emptyList()
)

@Serializable
data class NutritionCalculateResponse(
    @SerialName("nutrition") val nutrition: NutritionFacts? = null
)

@Serializable
data class NutritionCalculateRequest(
    @SerialName("drinkId") val drinkId: Int,
    @SerialName("selections") val selections: List<DrinkSelection> = emptyList()
)

// Per-ingredient nutrition facts (serving-based), used for local computation on
// the customize screen. Mirrors the backend ingredient_nutrition row.
@Serializable
data class IngredientNutrition(
    @SerialName("ingredientId") val ingredientId: Int? = null,
    @SerialName("servingSizeQty") val servingSizeQty: String? = null,
    @SerialName("servingSizeUnit") val servingSizeUnit: String? = null,
    @SerialName("calories") val calories: String? = null,
    @SerialName("protein") val protein: String? = null,
    @SerialName("totalCarbs") val totalCarbs: String? = null,
    @SerialName("dietaryFiber") val dietaryFiber: String? = null,
    @SerialName("totalSugars") val totalSugars: String? = null,
    @SerialName("addedSugars") val addedSugars: String? = null,
    @SerialName("totalFat") val totalFat: String? = null,
    @SerialName("saturatedFat") val saturatedFat: String? = null,
    @SerialName("transFat") val transFat: String? = null,
    @SerialName("cholesterol") val cholesterol: String? = null,
    @SerialName("sodium") val sodium: String? = null,
    @SerialName("caffeine") val caffeine: String? = null,
    @SerialName("allergens") val allergens: List<String> = emptyList()
)

@Serializable
data class NutritionIngredientsResponse(
    @SerialName("drinkId") val drinkId: Int? = null,
    @SerialName("ingredients") val ingredients: Map<String, IngredientNutrition> = emptyMap()
)

@Serializable
data class NutritionGoals(
    @SerialName("dailyCalorieGoal") val dailyCalorieGoal: Int? = null,
    @SerialName("dailyCaffeineLimit") val dailyCaffeineLimit: Int? = null,
    @SerialName("dailySugarLimit") val dailySugarLimit: Int? = null,
    @SerialName("dailyProteinGoal") val dailyProteinGoal: Int? = null,
    @SerialName("dailyCarbLimit") val dailyCarbLimit: Int? = null,
    @SerialName("dailyFatLimit") val dailyFatLimit: Int? = null,
    @SerialName("dietaryPreferences") val dietaryPreferences: List<String> = emptyList()
)

@Serializable
data class NutritionToday(
    @SerialName("calories") val calories: Double? = null,
    @SerialName("protein") val protein: Double? = null,
    @SerialName("totalCarbs") val totalCarbs: Double? = null,
    @SerialName("dietaryFiber") val dietaryFiber: Double? = null,
    @SerialName("totalSugars") val totalSugars: Double? = null,
    @SerialName("addedSugars") val addedSugars: Double? = null,
    @SerialName("totalFat") val totalFat: Double? = null,
    @SerialName("saturatedFat") val saturatedFat: Double? = null,
    @SerialName("transFat") val transFat: Double? = null,
    @SerialName("cholesterol") val cholesterol: Double? = null,
    @SerialName("sodium") val sodium: Double? = null,
    @SerialName("caffeine") val caffeine: Double? = null,
    @SerialName("itemsCount") val itemsCount: Int? = null,
    @SerialName("allergensConsumed") val allergensConsumed: List<String> = emptyList()
)

@Serializable
data class NutritionSummaryResponse(
    @SerialName("goals") val goals: NutritionGoals? = null,
    @SerialName("today") val today: NutritionToday? = null
)

@Serializable
data class NutritionHistoryBucket(
    @SerialName("period") val period: String? = null,
    @SerialName("start") val start: String? = null,
    @SerialName("totals") val totals: NutritionFacts? = null,
    @SerialName("itemsCount") val itemsCount: Int? = null,
    @SerialName("drinks") val drinks: Map<String, Int> = emptyMap()
)

@Serializable
data class NutritionHistoryResponse(
    @SerialName("period") val period: String? = null,
    @SerialName("series") val series: List<NutritionHistoryBucket> = emptyList()
)
