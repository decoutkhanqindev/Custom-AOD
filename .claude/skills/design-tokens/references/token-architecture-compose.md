# Token Architecture — Kotlin Compose

3-layer system mapping design decisions to Compose code patterns.

## Layer 1: Primitive Tokens

Raw values without semantic meaning. Kotlin `object` with `val` properties.

```kotlin
object AppPrimitiveColors {
    val Blue500 = Color(0xFF3B82F6)
    val Blue600 = Color(0xFF2563EB)
    // Full scale: 50, 100, 200, ..., 900
}

object AppPrimitiveSpacing {
    val Xs = 4.dp
    val Sm = 10.dp
    val Md = 16.dp
}
```

**Rules:**

- `object` (singleton, no state)
- PascalCase properties: `{ColorName}{Scale}` (e.g., `Blue500`)
- No `@Immutable` needed (object with val = inherently stable)
- One object per category: Colors, Spacing, Shape, Motion, Opacity, Elevation, Border, IconSize

## Layer 2: Semantic Tokens

Purpose-based aliases referencing primitives. `@Immutable data class` + dark/light instances.

```kotlin
@Immutable
data class AppColorTokens(
    val primary: Color,
    val onPrimary: Color,
    val background: Color,
    // ...
)

val darkAppColors = AppColorTokens(
    primary = AppPrimitiveColors.Purple500,
    onPrimary = AppPrimitiveColors.White,
    // ...
)
val lightAppColors = AppColorTokens(
    primary = AppPrimitiveColors.Purple600,
    // ...
)

val LocalAppColors = staticCompositionLocalOf { darkAppColors }
```

**Rules:**

- `@Immutable data class` (Compose stability)
- camelCase properties: `primary`, `onPrimary`, `surfaceVariant`
- Dark + Light instances for color tokens
- Single default instance for non-color tokens (spacing, shape, motion)
- `staticCompositionLocalOf` (not `compositionLocalOf` — tokens rarely change)

## Layer 3: Component Tokens

Component-specific overrides referencing semantic + primitive layers.

```kotlin
@Immutable
data class AppDialogTokens(
    val containerShape: Shape,
    val containerColor: Color,
    val contentPadding: Dp,
    // ...
)

val defaultAppDialog = AppDialogTokens(
    containerShape = AppPrimitiveShape.Lg,
    containerColor = AppPrimitiveColors.White,
    contentPadding = AppPrimitiveSpacing.Md,
)

val LocalAppDialog = staticCompositionLocalOf { defaultAppDialog }
```

**Rules:**

- Same pattern as semantic, but scoped to one component
- Properties named `{aspect}`: `containerShape`, `contentPadding`, `borderColor`
- Reference primitives directly (not semantic) for component-level control

## AppTheme Registration

Every token must register in two places:

### 1. CompositionLocalProvider (AppTheme composable)

```kotlin
CompositionLocalProvider(
    LocalAppNewToken provides defaultAppNewToken,
) { ... }
```

### 2. AppTheme accessor object

```kotlin
object AppTheme {
    val newToken: AppNewTokens
        @Composable get() = LocalAppNewToken.current
}
```

## Usage in Composables

```kotlin
// Access via AppTheme object
val color = AppTheme.colors.primary
val padding = AppTheme.spacing.screenPadding
val shape = AppTheme.dialog.containerShape
```

## Existing Token Inventory

| Layer     | Class                 | Properties                            |
|-----------|-----------------------|---------------------------------------|
| Primitive | AppPrimitiveColors    | ~80 colors (10 scales)                |
| Primitive | AppPrimitiveSpacing   | 18 sizes (1-140dp)                    |
| Primitive | AppPrimitiveShape     | 9 shapes (None-Full)                  |
| Primitive | AppPrimitiveMotion    | 4 durations + 3 easings               |
| Primitive | AppPrimitiveElevation | elevation values                      |
| Primitive | AppPrimitiveOpacity   | opacity values                        |
| Primitive | AppPrimitiveBorder    | border values                         |
| Primitive | AppPrimitiveIconSize  | icon sizes                            |
| Semantic  | AppColorTokens        | 28 colors (dark+light)                |
| Semantic  | AppSpacingTokens      | 8 spacings                            |
| Semantic  | AppShapeTokens        | shape aliases                         |
| Semantic  | AppElevationTokens    | elevation aliases                     |
| Semantic  | AppTypographyTokens   | 21 text styles                        |
| Semantic  | AppMotionTokens       | 4 durations + 3 easings               |
| Semantic  | AppOpacityTokens      | opacity aliases                       |
| Semantic  | AppDomainColorTokens  | domain colors (e.g. AOD clock colors) |
| Component | AppDialogTokens       | 8 properties                          |
| Component | AppBottomSheetTokens  | 7 properties                          |
| Component | AppCardTokens         | 9 properties                          |
