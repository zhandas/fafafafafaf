package com.example.mealjsonexample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import coil3.compose.AsyncImage


@Composable
fun Navigation(
    modifier: Modifier,
    navigationController: NavHostController,
) {
    val viewModel: MealsViewModel = viewModel()
    NavHost(
        modifier = modifier,
        navController = navigationController,
        startDestination = Graph.mainScreen.route
    ) {
        composable(route = Graph.mainScreen.route) {
            MainScreen(viewModel, navigationController)
        }
        composable(route = Graph.secondScreen.route) {
            SecondScreen(viewModel, navigationController)
        }
        composable(
            route = "${Graph.lastScreen.route}/{mealId}",
            arguments = listOf(navArgument("mealId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mealId = backStackEntry.arguments?.getString("mealId")
            mealId?.let { viewModel.getMealDetails(it) }
            MealDetailsScreen(viewModel) {
                navigationController.popBackStack()
            }
        }
    }
}

@Composable
fun SecondScreen(viewModel: MealsViewModel, navigationController:NavHostController)
{
    val categoryName = viewModel.chosenCategoryName.collectAsState()
    val dishesState = viewModel.mealsState.collectAsState()
    viewModel.getAllDishesByCategoryName(categoryName.value)
    Column{
        if (dishesState.value.isLoading){
            LoadingScreen()
        }
        if (dishesState.value.isError){
            ErrorScreen(dishesState.value.error!!)
        }
        if (dishesState.value.result.isNotEmpty()){
            DishesScreen(viewModel, navigationController)
        }
    }
}

@Composable
fun DishesScreen(viewModel: MealsViewModel, navigationController: NavHostController) {
    val dishesState by viewModel.mealsState.collectAsState()

    when {
        dishesState.isLoading -> {
            LoadingScreen()
        }
        dishesState.isError -> {
            ErrorScreen(dishesState.error ?: "Unknown error")
        }
        dishesState.result.isNotEmpty() -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(dishesState.result) { meal ->
                    DishItem(meal, navigationController)
                }
            }
        }
        else -> {
            // Handle the case where the result is empty
            Text("No dishes found")
        }
    }
}

@Composable
fun DishItem(meal: Meal, navigationController: NavHostController) {
    Box(
        modifier = Modifier
            .height(200.dp)
            .background(color = Color(0xFFE91E63))
            .clickable {
                navigationController.navigate("${Graph.lastScreen.route}/${meal.idMeal}")
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = meal.strMealThumb,
                contentDescription = null
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = meal.mealName,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
fun MainScreen(viewModel: MealsViewModel, navigationController: NavHostController){


    val categoriesState = viewModel.categoriesState.collectAsState()

    if (categoriesState.value.isLoading){
        LoadingScreen()
    }
    if (categoriesState.value.isError){
        ErrorScreen(categoriesState.value.error!!)
    }
    if (categoriesState.value.result.isNotEmpty()){
        CategoriesScreen(viewModel, categoriesState.value.result, navigationController)
    }

}

@Composable
fun CategoriesScreen(viewModel: MealsViewModel, result: List<Category>, navigationController: NavHostController) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2)
    ) {
        items(result){
            CategoryItem(viewModel, it, navigationController)
        }
    }
}

@Composable
fun CategoryItem(viewModel: MealsViewModel, category: Category, navigationController: NavHostController) {
    Box(
        modifier = Modifier
            .height(200.dp)
            .background(color = Color(0xFFE91E63))
            .clickable {
                viewModel.setChosenCategory(category.strCategory)
                navigationController.navigate("${Graph.secondScreen.route}")
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = category.strCategoryThumb,
                contentDescription = null
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = category.strCategory,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
fun ErrorScreen(error: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = error
        )
    }
}

@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

    @Composable
    fun MealDetailsScreen(viewModel: MealsViewModel, onBackClick: () -> Unit) {
        val mealDetails by viewModel.selectedMealDetails.collectAsState()

        mealDetails?.let { details ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .background(color = Color(0xFFE91E63))
            ) {
                AsyncImage(
                    model = details.strMealThumb,
                    contentDescription = details.strMeal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = details.strMeal,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Category: ${details.strCategory}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Area: ${details.strArea}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Instructions:",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = details.strInstructions,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFFE91E63)
                    )
                ) {
                    Text("Back")
                }
            }
        } ?: run {
            // Handle null mealDetails
        }
    }



