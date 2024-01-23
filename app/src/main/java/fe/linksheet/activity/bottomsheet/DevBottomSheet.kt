package fe.linksheet.activity.bottomsheet

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.res.Configuration
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import fe.android.preference.helper.compose.StatePreference
import fe.android.preference.helper.compose.mock.MockRepositoryState
import fe.linksheet.R
import fe.linksheet.activity.AppListModifier
import fe.linksheet.activity.BottomSheetActivity
import fe.linksheet.composable.util.BottomDrawer
import fe.linksheet.composable.util.defaultRoundedCornerShape
import fe.linksheet.extension.android.setText
import fe.linksheet.extension.android.shareUri
import fe.linksheet.extension.compose.currentActivity
import fe.linksheet.extension.compose.nullClickable
import fe.linksheet.extension.compose.runIf
import fe.linksheet.module.database.entity.LibRedirectDefault
import fe.linksheet.module.downloader.Downloader
import fe.linksheet.module.resolver.LibRedirectResolver
import fe.linksheet.module.viewmodel.BottomSheetViewModel
import fe.linksheet.resolver.BottomSheetResult
import fe.linksheet.resolver.DisplayActivityInfo
import fe.linksheet.ui.AppTheme
import fe.linksheet.ui.HkGroteskFontFamily
import fe.linksheet.ui.Theme
import fe.linksheet.util.PrivateBrowsingBrowser
import fe.linksheet.util.selfIntent
import kotlinx.coroutines.launch
import kotlin.math.ceil

class DevBottomSheet(
    activity: BottomSheetActivity,
    viewModel: BottomSheetViewModel
) : BottomSheet(activity, viewModel) {

    @Composable
    override fun ShowSheet(bottomSheetViewModel: BottomSheetViewModel) {
        AppTheme {
            BottomSheet(bottomSheetViewModel)
        }
    }

    companion object {
        val utilButtonWidth = 80.dp
        val buttonPadding = 15.dp

        val buttonRowHeight = 50.dp

        val appListItemPadding = 10.dp
        val appListItemHeight = 40.dp
        val preferredAppItemHeight = 60.dp

        val gridSize = 120.dp
        val gridItemHeightPackageText = 30.dp
        val gridItemHeightPrivateButton = 40.dp

        //            + 50.dp
        var gridItemHeight = 60.dp
//          + 50.dp


        // token from androidx.compose.material.ModelBottomSheet
        val maxModalBottomSheetWidth = 640.dp
    }


    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
    @Composable
    private fun BottomSheet(bottomSheetViewModel: BottomSheetViewModel, ) {
        val isBlackTheme = bottomSheetViewModel.theme.value == Theme.AmoledBlack
        val configuration = LocalConfiguration.current
        val landscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val result = bottomSheetViewModel.resolveResult

        val coroutineScope = rememberCoroutineScope()

        val drawerState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        LaunchedEffect(key1 = Unit) {
            drawerState.expand()
        }
        LaunchedEffect(drawerState.currentValue) {
            if (drawerState.currentValue == SheetValue.Hidden) {
                finish()
            }
        }

        val interactionSource = remember { MutableInteractionSource() }
        BottomDrawer(modifier = Modifier
            .runIf(landscape) {
                it
                    .fillMaxWidth(0.55f)
                    .fillMaxHeight()
            }
            .nullClickable(),
            isBlackTheme = isBlackTheme,
            drawerState = drawerState,
            shape = ShapeDefaults.Large,
            sheetContent = {
                SheetContent(result = result, landscape = landscape, hideDrawer = {
                    coroutineScope.launch { drawerState.hide() }
                })
            })
    }

    @Composable
    private fun SheetContent(
        result: BottomSheetResult?, landscape: Boolean, hideDrawer: () -> Unit
    ) {
        if (result != null && result is BottomSheetResult.BottomSheetSuccessResult && !result.hasAutoLaunchApp) {
            val showPackage = remember {
                result.showExtended || viewModel.alwaysShowPackageName.value
            }


            val maxHeight = (if (landscape) LocalConfiguration.current.screenWidthDp
            else LocalConfiguration.current.screenHeightDp) / 3f

            val itemHeight = if (viewModel.gridLayout.value) {
                val gridItemHeight = gridItemHeight.value + if (showPackage) 10f else 0.0f

                gridItemHeight
            } else appListItemHeight.value

            val baseHeight = ((ceil((maxHeight / itemHeight).toDouble()) - 1) * itemHeight).dp

            if (result.filteredItem == null) {
                OpenWith(
                    bottomSheetViewModel = viewModel,
                    hideDrawer = hideDrawer,
                    showPackage = showPackage,
                    previewUrl = viewModel.previewUrl.value
                )
            } else {
                OpenWithPreferred(
                    bottomSheetViewModel = viewModel,
                    hideDrawer = hideDrawer,
                    showPackage = showPackage,
                    previewUrl = viewModel.previewUrl.value
                )
            }
        } else {
            val hasNoHandlers = result is BottomSheetResult.BottomSheetNoHandlersFound

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (hasNoHandlers) {
                    Text(
                        text = stringResource(id = R.string.no_handlers_found),
                        fontFamily = HkGroteskFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = stringResource(id = R.string.no_handlers_found_explainer),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.loading_link),
                        fontFamily = HkGroteskFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (!hasNoHandlers) {
                    Spacer(modifier = Modifier.height(10.dp))
                    CircularProgressIndicator()
                }
            }

            if (hasNoHandlers) {
                val padding = PaddingValues(horizontal = 10.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = buttonRowHeight)
                        .padding(buttonPadding)
                ) {
                    CopyButton(
                        result,
                        hideDrawer,
                        isTextBasedButton = viewModel.useTextShareCopyButtons.value
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    ShareToButton(
                        result,
                        isTextBasedButton = viewModel.useTextShareCopyButtons.value
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun CopyButton(
        result: BottomSheetResult?,
        hideDrawer: () -> Unit,
        modifier: Modifier = Modifier,
        isTextBasedButton: Boolean
    ) {
        if (!isTextBasedButton) {
            ElevatedButton(
                modifier = modifier,
                onClick = {
                    viewModel.clipboardManager.setText(
                        "URL", result?.uri.toString()
                    )

                    if (!viewModel.urlCopiedToast.value) {
                        showToast(R.string.url_copied)
                    }

                    if (viewModel.hideAfterCopying.value) {
                        hideDrawer()
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy, contentDescription = null
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = stringResource(id = R.string.copy_url),
                    fontFamily = HkGroteskFontFamily,
                    maxLines = 1,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            TextButton(
                modifier = modifier,
                onClick = {
                    viewModel.clipboardManager.setText(
                        "URL", result?.uri.toString()
                    )

                    if (!viewModel.urlCopiedToast.value) {
                        showToast(R.string.url_copied)
                    }

                    if (viewModel.hideAfterCopying.value) {
                        hideDrawer()
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy, contentDescription = null
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = stringResource(id = R.string.copy_url),
                    fontFamily = HkGroteskFontFamily,
                    maxLines = 1,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    @Composable
    private fun ShareToButton(
        result: BottomSheetResult?, modifier: Modifier = Modifier, isTextBasedButton: Boolean
    ) {
        if (!isTextBasedButton) {
            ElevatedButton(modifier = modifier, onClick = {
                startActivity(shareUri(result?.uri))
                finish()
            }) {
                Icon(
                    imageVector = Icons.Default.Share, contentDescription = null
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = stringResource(id = R.string.send_to),
                    fontFamily = HkGroteskFontFamily,
                    maxLines = 1,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            TextButton(modifier = modifier, onClick = {
                startActivity(shareUri(result?.uri))
                finish()
            }) {
                Icon(
                    imageVector = Icons.Default.Share, contentDescription = null
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = stringResource(id = R.string.send_to),
                    fontFamily = HkGroteskFontFamily,
                    maxLines = 1,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }


    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun OpenWithPreferred(
        bottomSheetViewModel: BottomSheetViewModel,
        hideDrawer: () -> Unit,
        showPackage: Boolean,
        previewUrl: Boolean
    ) {
        if (bottomSheetViewModel.gridLayout.value) {
            BtmSheetGridUI(
                bottomSheetViewModel = bottomSheetViewModel,
                hideDrawer = hideDrawer,
                showPackage = showPackage,
                previewUrl = previewUrl,
                forPreferred = true
            )
        } else {
            BtmSheetNonGridUI(
                bottomSheetViewModel = bottomSheetViewModel,
                hideDrawer = hideDrawer,
                showPackage = showPackage,
                previewUrl = previewUrl,
                forPreferred = true
            )
        }
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    private fun OpenWith(
        bottomSheetViewModel: BottomSheetViewModel,
        hideDrawer: () -> Unit,
        showPackage: Boolean,
        previewUrl: Boolean,
    ) {
        if (bottomSheetViewModel.gridLayout.value) {
            BtmSheetGridUI(
                bottomSheetViewModel = bottomSheetViewModel,
                hideDrawer = hideDrawer,
                showPackage = showPackage,
                previewUrl = previewUrl,
                forPreferred = false
            )
        } else {
            BtmSheetNonGridUI(
                bottomSheetViewModel = bottomSheetViewModel,
                hideDrawer = hideDrawer,
                showPackage = showPackage,
                previewUrl = previewUrl,
                forPreferred = false
            )
        }
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    @Composable
    private fun BtmSheetNonGridUI(
        bottomSheetViewModel: BottomSheetViewModel,
        hideDrawer: () -> Unit,
        showPackage: Boolean,
        previewUrl: Boolean,
        forPreferred: Boolean
    ) {
        val result = bottomSheetViewModel.resolveResult!!
        if (result !is BottomSheetResult.BottomSheetSuccessResult) return
        if (result.isEmpty) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.no_app_to_handle_link_found)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            return
        }
        if (forPreferred) {
            val filteredItem = result.filteredItem!!
            LaunchedEffect(key1 = filteredItem) {
                bottomSheetViewModel.appInfo.value = filteredItem
                bottomSheetViewModel.privateBrowser.value = shouldShowRequestPrivate(filteredItem)
            }
        }

        var selectedItem by remember { mutableIntStateOf(-1) }
        val modifier: AppListModifier = @Composable { index, info ->
            Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, end = 15.dp)
                .clip(defaultRoundedCornerShape)
                .combinedClickable(onClick = {
                    bottomSheetViewModel.privateBrowser.value = shouldShowRequestPrivate(info)
                    bottomSheetViewModel.appInfo.value = info
                    if (forPreferred) {
                        launchApp(result, info)
                    } else {
                        if (bottomSheetViewModel.singleTap.value) {
                            launchApp(result, info)
                        } else {
                            if (selectedItem == index) launchApp(result, info)
                            else selectedItem = index
                        }
                    }
                }, onDoubleClick = {
                    if (!bottomSheetViewModel.singleTap.value) {
                        launchApp(result, info)
                    } else {
                        startPackageInfoActivity(info)
                    }
                }, onLongClick = {
                    if (bottomSheetViewModel.singleTap.value) {
                        selectedItem = index
                    } else {
                        startPackageInfoActivity(info)
                    }
                })
                .background(
                    if (selectedItem == index) LocalContentColor.current.copy(0.1f)
                    else Color.Transparent
                )
                .padding(appListItemPadding)
        }
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            if (previewUrl && result.uri != null) {
                item(key = "previewUrl") {
                    UrlBar(
                        uri = result.uri,
                        clipboardManager = viewModel.clipboardManager,
                        urlCopiedToast = viewModel.urlCopiedToast,
                        hideAfterCopying = viewModel.hideAfterCopying,
                        showToast = {
                            showToast(it)
                        },
                        hideDrawer = hideDrawer,
                        shareUri = {
                            startActivity(shareUri(result.uri))
                            finish()
                        }
                    )
                }
            }

            if (forPreferred) {
                val filteredItem = result.filteredItem!!
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp, end = 10.dp)
                            .clip(defaultRoundedCornerShape)
                            .clickable {
                                launchApp(result, filteredItem, always = false)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 5.dp, end = 5.dp)
                                .heightIn(min = preferredAppItemHeight),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    bitmap = filteredItem.iconBitmap,
                                    contentDescription = filteredItem.label,
                                    modifier = Modifier.size(32.dp)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = stringResource(
                                            id = R.string.open_with_app,
                                            filteredItem.label,
                                        ),
                                        fontFamily = HkGroteskFontFamily,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    if (showPackage) {
                                        Text(
                                            text = filteredItem.packageName, fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                                FilledTonalIconButton(onClick = {

                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Shield,
                                        contentDescription = stringResource(id = R.string.request_private_browsing)
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.width(10.dp))
                }

                item {
                    ButtonColumn(
                        bottomSheetViewModel = bottomSheetViewModel,
                        enabled = true,
                        uri = result.uri,
                        onClick = { launchApp(result, filteredItem, always = it) },
                        hideDrawer = hideDrawer
                    )
                }
//                item {
//                    HorizontalDivider(
//                        modifier = Modifier.padding(
//                            start = 25.dp, end = 25.dp, top = 5.dp, bottom = 5.dp
//                        ), color = MaterialTheme.colorScheme.outline.copy(0.25f)
//                    )
//                }
                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp),
                        color = MaterialTheme.colorScheme.outline.copy(0.25f)
                    )
                }
                item {
                    Text(
                        modifier = Modifier.padding(start = 15.dp),
                        text = stringResource(id = R.string.use_a_different_app),
                        fontFamily = HkGroteskFontFamily,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(5.dp))
                }
            } else {
                item(key = R.string.open_with) {
                    Text(
                        text = stringResource(id = R.string.open_with),
                        fontFamily = HkGroteskFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(
                            start = 15.dp, top = if (previewUrl) 10.dp else 0.dp
                        )
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
            // App List Function:
            itemsIndexed(
                items = result.resolved,
                key = { _, item -> item.flatComponentName }) { index, info ->
                Column {
//                    if (shouldShowRequestPrivate(info) != null && index != 0) {
//                        HorizontalDivider(
//                            modifier = Modifier.padding(
//                                start = 25.dp, end = 25.dp
//                            ), color = MaterialTheme.colorScheme.outline.copy(0.25f)
//                        )
//                    }
                    Row(
                        modifier = modifier(index, info).wrapContentHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                bitmap = info.iconBitmap,
                                contentDescription = info.label,
                                modifier = Modifier.size(32.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(text = info.label)
                                if (showPackage) {
                                    Text(
                                        text = info.packageName,
                                        fontSize = 12.sp,
                                        lineHeight = 12.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (selectedItem == index && !forPreferred) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.align(Alignment.CenterEnd)
                                    )
                                }
                            }
                        }
                    }
//                    if (shouldShowRequestPrivate(info) != null) {
//                        ElevatedButton(
//                            onClick = {
//                                launchApp(
//                                    result,
//                                    info = info,
//                                    privateBrowsingBrowser = shouldShowRequestPrivate(info)
//                                )
//                            },
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(start = 20.dp, end = 20.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Shield,
//                                contentDescription = null,
//                                modifier = Modifier.size(20.dp)
//                            )
//                            Spacer(modifier = Modifier.width(5.dp))
//                            Text(
//                                text = stringResource(id = R.string.request_private_browsing),
//                                textAlign = TextAlign.Center,
//                                fontFamily = HkGroteskFontFamily,
//                                fontWeight = FontWeight.SemiBold
//                            )
//                        }
//                        HorizontalDivider(
//                            modifier = Modifier.padding(
//                                start = 25.dp, end = 25.dp, top = 10.dp
//                            ), color = MaterialTheme.colorScheme.outline.copy(0.25f)
//                        )
//                    }
                }
            }
            if (!forPreferred) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                item {
                    ButtonColumn(
                        bottomSheetViewModel = bottomSheetViewModel,
                        enabled = selectedItem != -1,
                        uri = null,
                        onClick = { always ->
                            launchApp(
                                result, result.resolved[selectedItem], always
                            )
                        },
                        hideDrawer = hideDrawer
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun BtmSheetGridUI(
        bottomSheetViewModel: BottomSheetViewModel,
        hideDrawer: () -> Unit,
        showPackage: Boolean,
        previewUrl: Boolean,
        forPreferred: Boolean
    ) {
        val result = bottomSheetViewModel.resolveResult!!
        if (result !is BottomSheetResult.BottomSheetSuccessResult) return
        if (result.isEmpty) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.no_app_to_handle_link_found)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            return
        }
        var selectedItem by remember { mutableIntStateOf(-1) }
        val doesSelectedBrowserSupportsIncognitoLaunch = rememberSaveable {
            mutableStateOf(false)
        }
        val modifier: AppListModifier = @Composable { index, info ->
            Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp)
                .clip(defaultRoundedCornerShape)
                .combinedClickable(onClick = {
                    bottomSheetViewModel.privateBrowser.value = shouldShowRequestPrivate(info)
                    bottomSheetViewModel.appInfo.value = info
                    doesSelectedBrowserSupportsIncognitoLaunch.value =
                        bottomSheetViewModel.privateBrowser.value != null
                    if (bottomSheetViewModel.privateBrowser.value == null) {
                        if (bottomSheetViewModel.singleTap.value || forPreferred) {
                            launchApp(result, info)
                        } else {
                            if (selectedItem == index) launchApp(result, info)
                            else selectedItem = index
                        }
                    }
                }, onDoubleClick = {
                    doesSelectedBrowserSupportsIncognitoLaunch.value =
                        shouldShowRequestPrivate(info) != null
                    if (!bottomSheetViewModel.singleTap.value && !doesSelectedBrowserSupportsIncognitoLaunch.value) {
                        launchApp(result, info)
                    } else {
                        startPackageInfoActivity(info)
                    }
                }, onLongClick = {
                    if (bottomSheetViewModel.singleTap.value) {
                        selectedItem = index
                    } else {
                        startPackageInfoActivity(info)
                    }
                })
                .background(
                    if (selectedItem == index) LocalContentColor.current.copy(
                        0.1f
                    ) else Color.Transparent
                )
                .padding(appListItemPadding)
        }
        if (forPreferred) {
            val filteredItem = result.filteredItem!!
            LaunchedEffect(key1 = filteredItem) {
                bottomSheetViewModel.appInfo.value = filteredItem
                bottomSheetViewModel.privateBrowser.value = shouldShowRequestPrivate(filteredItem)
            }
        }

        // https://stackoverflow.com/questions/69382494/jetpack-compose-vertical-grid-single-item-span-size
        LazyVerticalGrid(columns = GridCells.Adaptive(85.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .animateContentSize(),
            content = {
                item(key = "previewUrl", span = { GridItemSpan(maxCurrentLineSpan) }) {
                    if (previewUrl && result.uri != null) {
                        UrlBar(
                            uri = result.uri,
                            clipboardManager = viewModel.clipboardManager,
                            urlCopiedToast = viewModel.urlCopiedToast,
                            hideAfterCopying = viewModel.hideAfterCopying,
                            showToast = {
                                showToast(it)
                            },
                            hideDrawer = hideDrawer,
                            shareUri = {
                                startActivity(shareUri(result.uri))
                                finish()
                            }
                        )
                    }
                }
                if (doesSelectedBrowserSupportsIncognitoLaunch.value) {
                    item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                bottomSheetViewModel.appInfo.value?.let {
                                    launchApp(
                                        result,
                                        it, always = false
                                    )
                                }
                            }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 15.dp, end = 15.dp)
                                    .heightIn(min = preferredAppItemHeight),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    bitmap = bottomSheetViewModel.appInfo.value!!.iconBitmap,
                                    contentDescription = bottomSheetViewModel.appInfo.value!!.label,
                                    modifier = Modifier.size(32.dp)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = stringResource(
                                            id = R.string.open_with_app,
                                            bottomSheetViewModel.appInfo.value!!.label,
                                        ),
                                        fontFamily = HkGroteskFontFamily,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (showPackage) {
                                        Text(
                                            text = bottomSheetViewModel.appInfo.value!!.packageName,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                            Button(
                                onClick = {
                                    bottomSheetViewModel.appInfo.value?.let {
                                        launchApp(
                                            result,
                                            info = it,
                                            privateBrowsingBrowser = bottomSheetViewModel.privateBrowser.value
                                        )
                                    }
                                }, modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 15.dp, end = 15.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield, contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = stringResource(id = R.string.request_private_browsing),
                                    textAlign = TextAlign.Center,
                                    fontFamily = HkGroteskFontFamily,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Button(
                                onClick = {
                                    bottomSheetViewModel.appInfo.value?.let {
                                        launchApp(
                                            result,
                                            info = it
                                        )
                                    }
                                }, modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 15.dp, end = 15.dp)
                            ) {
                                Text(
                                    text = "Open in Standard Mode",
                                    textAlign = TextAlign.Center,
                                    fontFamily = HkGroteskFontFamily,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    return@LazyVerticalGrid
                }
                if (forPreferred) {
                    val filteredItem = result.filteredItem!!
                    item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                launchApp(result, filteredItem, always = false)
                            }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 15.dp, end = 15.dp)
                                    .heightIn(min = preferredAppItemHeight),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    bitmap = filteredItem.iconBitmap,
                                    contentDescription = filteredItem.label,
                                    modifier = Modifier.size(32.dp)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = stringResource(
                                            id = R.string.open_with_app,
                                            filteredItem.label,
                                        ),
                                        fontFamily = HkGroteskFontFamily,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (showPackage) {
                                        Text(
                                            text = filteredItem.packageName, fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                        ButtonColumn(
                            bottomSheetViewModel = bottomSheetViewModel,
                            enabled = true,
                            uri = result.uri,
                            onClick = { launchApp(result, filteredItem, always = it) },
                            hideDrawer = hideDrawer
                        )
                    }
                    item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                        HorizontalDivider(
                            modifier = Modifier.padding(
                                start = 25.dp, end = 25.dp, top = 5.dp, bottom = 5.dp
                            ), color = MaterialTheme.colorScheme.outline.copy(0.25f)
                        )
                    }
                    item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                        Text(
                            modifier = Modifier.padding(start = 15.dp),
                            text = stringResource(id = R.string.use_a_different_app),
                            fontFamily = HkGroteskFontFamily,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                } else {
                    item(key = R.string.open_with, span = { GridItemSpan(maxCurrentLineSpan) }) {
                        Text(
                            text = stringResource(id = R.string.open_with),
                            fontFamily = HkGroteskFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(
                                start = 15.dp, top = if (previewUrl) 10.dp else 0.dp
                            )
                        )
                    }
                    item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
                itemsIndexed(
                    items = result.resolved,
                    key = { _, item -> item.flatComponentName }) { index, info ->
                    Column(
                        modifier = modifier(
                            index, info
                        ), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            bitmap = info.iconBitmap,
                            contentDescription = info.label,
                            modifier = Modifier.size(32.dp)
                        )

                        Text(
                            text = info.label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 3.dp)
                        )

                        if (showPackage) {
                            Text(
                                text = info.packageName,
                                fontSize = 12.sp,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                    }
                }
                if (!forPreferred) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ButtonColumn(
                            bottomSheetViewModel = bottomSheetViewModel,
                            enabled = selectedItem != -1,
                            uri = null,
                            onClick = { always ->
                                launchApp(
                                    result, result.resolved[selectedItem], always
                                )
                            },
                            hideDrawer = hideDrawer
                        )
                    }
                }
            })
    }

    private fun shouldShowRequestPrivate(info: DisplayActivityInfo): PrivateBrowsingBrowser.Firefox? {
        if (!viewModel.enableRequestPrivateBrowsingButton.value) return null
        return PrivateBrowsingBrowser.getSupportedBrowser(info.packageName)
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun ButtonColumn(
        bottomSheetViewModel: BottomSheetViewModel,
        enabled: Boolean,
        uri: Uri?,
        onClick: (always: Boolean) -> Unit,
        hideDrawer: () -> Unit
    ) {
        val result = bottomSheetViewModel.resolveResult!!
        if (result !is BottomSheetResult.BottomSheetSuccessResult) return

        val utilButtonWidthSum = utilButtonWidth * listOf(
            bottomSheetViewModel.enableCopyButton.value,
            bottomSheetViewModel.enableSendButton.value,
            bottomSheetViewModel.enableIgnoreLibRedirectButton.value,
            result.downloadable.isDownloadable(),
            bottomSheetViewModel.enableRequestPrivateBrowsingButton.value
        ).count { it }

        val configuration = LocalConfiguration.current
        val landscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val widthHalf = if (landscape) {
            maxModalBottomSheetWidth
        } else LocalConfiguration.current.screenWidthDp.dp

        val useTwoRows = utilButtonWidthSum > widthHalf / 2
        val padding = PaddingValues(horizontal = 10.dp)
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
//                if (bottomSheetViewModel.enableCopyButton.value && !bottomSheetViewModel.previewUrl()) {
//                    CopyButton(
//                        modifier = Modifier
//                            .fillMaxWidth(if (bottomSheetViewModel.enableSendButton.value) 0.5f else 1f)
//                            .padding(start = 15.dp, end = 15.dp),
//                        result = result,
//                        hideDrawer = hideDrawer,
//                        isTextBasedButton = bottomSheetViewModel.useTextShareCopyButtons.value
//                    )
//                }
//                if (bottomSheetViewModel.enableSendButton.value) {
//                    ShareToButton(
//                        result = result,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(
//                                start = if (bottomSheetViewModel.enableCopyButton.value) 0.dp else 15.dp,
//                                end = 15.dp
//                            ),
//                        isTextBasedButton = bottomSheetViewModel.useTextShareCopyButtons.value
//                    )
//                }
            }
            if (result.downloadable.isDownloadable()) {
                Spacer(modifier = Modifier.height(5.dp))
                if (!bottomSheetViewModel.useTextShareCopyButtons.value) {
                    ElevatedButton(modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, end = 15.dp),
                        onClick = {
                            bottomSheetViewModel.startDownload(
                                resources,
                                result.uri,
                                result.downloadable as Downloader.DownloadCheckResult.Downloadable
                            )

                            if (!bottomSheetViewModel.downloadStartedToast.value) {
                                showToast(R.string.download_started)
                            }

                            hideDrawer()
                        }) {
                        Icon(
                            imageVector = Icons.Default.Download, contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(id = R.string.download),
                            fontFamily = HkGroteskFontFamily,
                            maxLines = 1,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    TextButton(modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, end = 15.dp),
                        onClick = {
                            bottomSheetViewModel.startDownload(
                                resources,
                                result.uri,
                                result.downloadable as Downloader.DownloadCheckResult.Downloadable
                            )

                            if (!bottomSheetViewModel.downloadStartedToast.value) {
                                showToast(R.string.download_started)
                            }

                            hideDrawer()
                        }) {
                        Icon(
                            imageVector = Icons.Default.Download, contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(id = R.string.download),
                            fontFamily = HkGroteskFontFamily,
                            maxLines = 1,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

            }
            if (useTwoRows) {
                OpenButtons(
                    bottomSheetViewModel = bottomSheetViewModel,
                    enabled = enabled,
                    onClick = onClick
                )
            }
            val libRedirectResult = result.libRedirectResult
            if (bottomSheetViewModel.enableIgnoreLibRedirectButton.value && libRedirectResult is LibRedirectResolver.LibRedirectResult.Redirected) {
                ElevatedOrTextButton(
                    textButton = bottomSheetViewModel.useTextShareCopyButtons.value, onClick = {
                        finish()
                        startActivity(
                            selfIntent(
                                libRedirectResult.originalUri,
                                bundleOf(LibRedirectDefault.libRedirectIgnore to true)
                            )
                        )
                    }, buttonText = R.string.ignore_libredirect
                )
            }


            if (!useTwoRows && bottomSheetViewModel.appInfo.value != null) {
                OpenButtons(
                    bottomSheetViewModel = bottomSheetViewModel,
                    enabled = enabled,
                    onClick = onClick
                )
            }
        }
    }

    @Composable
    private fun OpenButtons(
        bottomSheetViewModel: BottomSheetViewModel,
        enabled: Boolean,
        onClick: (always: Boolean) -> Unit
    ) {
        val activity = LocalContext.currentActivity()
        val result = bottomSheetViewModel.resolveResult!!
        if (result !is BottomSheetResult.BottomSheetSuccessResult) return
        if (!result.isEmpty) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .animateContentSize()
            ) {
//                HorizontalDivider(
//                    modifier = Modifier.padding(
//                        start = 25.dp, end = 25.dp, top = 5.dp, bottom = 5.dp
//                    ), color = MaterialTheme.colorScheme.outline.copy(0.25f)
//                )
//                if (bottomSheetViewModel.privateBrowser.value != null) {
//                    Button(
//                        enabled = enabled, onClick = {
//                            bottomSheetViewModel.appInfo.value?.let {
//                                launchApp(
//                                    result,
//                                    info = it,
//                                    privateBrowsingBrowser = bottomSheetViewModel.privateBrowser.value
//                                )
//                            }
//                        }, modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(start = 15.dp, end = 15.dp)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Shield, contentDescription = null,
//                            modifier = Modifier.size(20.dp)
//                        )
//                        Spacer(modifier = Modifier.width(5.dp))
//                        Text(
//                            text = stringResource(id = R.string.request_private_browsing),
//                            textAlign = TextAlign.Center,
//                            fontFamily = HkGroteskFontFamily,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                    }
//                }
                Button(
                    enabled = enabled,
                    onClick = { onClick(false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, end = 15.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.just_once),
                        fontFamily = HkGroteskFontFamily,
                        maxLines = 1,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Button(
                    enabled = enabled,
                    onClick = { onClick(true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, end = 15.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.always),
                        fontFamily = HkGroteskFontFamily,
                        maxLines = 1,
                        fontWeight = FontWeight.SemiBold
                    )
                }

            }
        } else {
            ElevatedOrTextButton(
                onClick = {
                    bottomSheetViewModel.startMainActivity(activity)
                },
                textButton = bottomSheetViewModel.useTextShareCopyButtons.value,
                buttonText = R.string.open_settings
            )
        }
    }

    @Composable
    private fun ElevatedOrTextButton(
        textButton: Boolean, onClick: () -> Unit, @StringRes buttonText: Int
    ) {
        if (textButton) TextButton(onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, end = 15.dp),
            content = {
                Text(
                    text = stringResource(id = buttonText),
                    fontFamily = HkGroteskFontFamily,
                    maxLines = 1,
                    fontWeight = FontWeight.SemiBold
                )
            })
        else ElevatedButton(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp),
            onClick = onClick,
            content = { Text(text = stringResource(id = buttonText)) })
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UrlBar(
    uri: Uri,
    clipboardManager: ClipboardManager,
    urlCopiedToast: StatePreference<Boolean>,
    hideAfterCopying: StatePreference<Boolean>,
    showToast: (Int) -> Unit,
    hideDrawer: () -> Unit,
    shareUri: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(start = 10.dp, end = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = uri.toString(),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
                lineHeight = 12.sp
            )

            Spacer(modifier = Modifier.width(10.dp))

            CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                IconButton(
                    onClick = {
                        clipboardManager.setText("URL", uri.toString())

                        if (urlCopiedToast()) {
                            showToast(R.string.url_copied)
                        }

                        if (hideAfterCopying()) {
                            hideDrawer()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(id = R.string.copy_url)
                    )
                }

                IconButton(onClick = shareUri) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(id = R.string.copy_url)
                    )
                }
            }
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp),
        color = MaterialTheme.colorScheme.outline.copy(0.25f)
    )
}

@Preview(name = "UrlPreview", showBackground = true)
@Composable
private fun UrlBarPreview() {
    val clipboardManager = LocalContext.current.getSystemService<ClipboardManager>()!!

    UrlBar(
        uri = Uri.parse("https://developer.android.com/jetpack/compose/text/configure-layout"),
        clipboardManager = clipboardManager,
        urlCopiedToast = MockRepositoryState.preference(true),
        hideAfterCopying = MockRepositoryState.preference(true),
        showToast = {},
        hideDrawer = {},
        shareUri = {}
    )
}