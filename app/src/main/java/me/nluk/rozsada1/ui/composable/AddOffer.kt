package me.nluk.rozsada1.ui.composable

import android.content.ContentResolver
import android.content.Context
import android.location.Address
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonNull.content
import me.nluk.rozsada1.R
import me.nluk.rozsada1.services.ImageUploadService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import android.webkit.MimeTypeMap
import androidx.compose.foundation.*
import androidx.compose.ui.text.style.TextAlign
import me.nluk.rozsada1.model.Location
import me.nluk.rozsada1.model.Offer
import me.nluk.rozsada1.model.OfferDraft


@ExperimentalPagerApi
@Composable
fun AddOfferScreen(model : AddOfferViewModel = hiltViewModel()){
    val inMaps = model.inMaps.value
    BackHandler(enabled = inMaps){
        model.exitMaps()
    }
    val addImagesLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        model.addImages(uris)
    }
    val addImages = { addImagesLauncher.launch("image/*") }
    if(inMaps){
        MapScreen(model::exitMaps, model::setAddress)
    }
    else{
        AddOfferScreenContent(
            offer = model.newOffer.value,
            offerOperations = OfferOperations(model::setOfferTitle, model::setOfferDescription),
            removed = model.removed.value,
            imageUris =  model.images.value,
            addImages = addImages,
            markAndRemove = model::markAndRemove,
            enterMaps = model::enterMaps,
            uploadImages = model::uploadImages
        )
    }
}

@Composable
@Preview
fun AddOfferScreenPreview(){
    AddOfferScreenContent(
        removed = null,
        offer = OfferDraft(),
        offerOperations = OfferOperations({}, {}),
        imageUris =  listOf(Uri.parse("content://com.android.providers.media.documents/document/image%3A32")),
        addImages = {},
        markAndRemove = {},
        enterMaps = {},
        uploadImages = {}
    )
}

@Composable
fun AddOfferScreenContent(
    removed : Int?,
    offer: OfferDraft,
    offerOperations: OfferOperations,
    imageUris : List<Uri>,
    addImages : () -> Unit,
    markAndRemove : (Int) -> Unit,
    enterMaps : () -> Unit,
    uploadImages : () -> Unit
){

    val rowState = rememberLazyListState()
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .scrollable(scrollState, Orientation.Vertical),
        horizontalAlignment = Alignment.CenterHorizontally) {

        TitleText(text = stringResource(R.string.create_offer))

        Card(modifier = Modifier
            .padding(12.dp)) {

            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {


                if (imageUris.isEmpty()) {
                    AddImageIcon {
                        addImages()
                    }
                    Text(text = stringResource(R.string.add_offer_images))
                } else {
                    LazyRow(
                        state = rowState,
                        content = {
                            item {
                                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                            }
                            itemsIndexed(imageUris) { index, item ->
                                Image(
                                    painter = rememberImagePainter(data = item),
                                    contentDescription = "offer_image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(150.dp)
                                        .aspectRatio(1.0f)
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onLongPress = {
                                                    markAndRemove(index)
                                                }
                                            )
                                        }
                                        .border(
                                            5.dp,
                                            if (removed == index) Color.Red else Color.Transparent
                                        )
                                        .padding(horizontal = 5.dp)
                                )

                            }
                            item {
                                AddImageIcon {
                                    addImages()
                                }
                            }
                            item {
                                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                            }
                        })
                }
                OfferData(offer = offer, setTitle = offerOperations.setTitle, setDescription = offerOperations.setDescription)
                Button(onClick = enterMaps) {
                    Text(text = "Set location")
                }
                Button(onClick = uploadImages) {
                    Text(text = "Upload images")
                }
            }
        }
    }
}


@Composable
fun OfferData(offer : OfferDraft, setTitle : (String) -> Unit, setDescription : (String) -> Unit){
    LabeledField(R.string.category) {
        var expanded by remember { mutableStateOf(false) }
        val items = listOf("A", "B", "C", "D", "E", "F")
        var selectedIndex by remember { mutableStateOf(0) }
        Box(modifier = Modifier
            .fillMaxWidth()) {
            Text(items[selectedIndex],modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(12.dp)
                .clickable(onClick = { expanded = true }))
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                items.forEachIndexed { index, s ->
                    DropdownMenuItem(onClick = {
                        selectedIndex = index
                        expanded = false
                    }) {
                        Text(text = s)
                    }
                }
            }
        }
    }
    LabeledField(R.string.offer_title) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = offer.title ?: "",
            placeholder = { Text(stringResource(R.string.ofer_title_placeholder)) },
            onValueChange = {
                setTitle(it)
            }
        )
    }
//    LabeledField(R.string.offer_description) {
//        TextField(
//            modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 80.dp),
//            value = offer.description ?: "",
//            placeholder = { Text(stringResource(R.string.offer_description_placeholder)) },
//            onValueChange = {
//                setDescription(it)
//            },
//        )
//    }
}

@Composable
fun LabeledField(label_res : Int, field : @Composable () -> Unit){
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 25.dp, bottom = 5.dp),
        text = stringResource(label_res),
        textAlign = TextAlign.Start
    )
    field()
}

@Composable
fun AddImageIcon(onClick : () -> Unit){
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .border(2.dp, Color.LightGray, RoundedCornerShape(6.dp))
            .width(150.dp)
            .aspectRatio(1.0f)
    ) {
        Icon(
            imageVector = Icons.Filled.AddPhotoAlternate, "add a photo", tint = Color.LightGray, modifier = Modifier.fillMaxSize()
        )
    }
}


@HiltViewModel
class AddOfferViewModel @Inject constructor(
    private val imageUploadService: ImageUploadService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var inMaps = mutableStateOf(false)
    var images = mutableStateOf(listOf<Uri>())
    var removed =  mutableStateOf<Int?>(null)
    var newOffer = mutableStateOf(OfferDraft())

    fun markAndRemove(index : Int) =  viewModelScope.launch(Dispatchers.IO) {
        removed.value = index
        delay(200L)
        val new = ArrayList(images.value)
        new.removeAt(index)
        removed.value = null
        images.value = new
    }

    fun addImages(newImages : List<Uri>){
        images.value = ArrayList(images.value) + newImages
    }

    fun exitMaps(){
        inMaps.value = false
    }

    fun enterMaps(){
        inMaps.value = true
    }

    fun uploadImages() = viewModelScope.launch(Dispatchers.IO) {
        for (imageUri in images.value){
            val contentBytes = context.contentResolver.openInputStream(imageUri)?.readBytes()
            if(contentBytes == null){
                println("Couldn't read content")
                continue
            }
            val uploadUrl = imageUploadService.getUploadUrl()
            println("Got url: $uploadUrl")
            val body = contentBytes.toRequestBody(getMimeType(imageUri)!!.toMediaType(), 0, contentBytes.size)
            imageUploadService.uploadFile(uploadUrl.uploadUrl, body)
            println("Uploaded!")
        }
    }

    fun getMimeType(uri: Uri): String? {
        return if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            context.contentResolver.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(
                uri.toString()
            )
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileExtension.lowercase()
            )
        }
    }

    fun setOfferTitle(title : String){
        newOffer.value = newOffer.value.copy(title = title)
    }

    fun setOfferDescription(description : String){
        newOffer.value = newOffer.value.copy(description =  description)
    }

    fun setAddress(address : Address){
        newOffer.value = newOffer.value.copy(
            city = address.getAddressLine(0),
            location = Location(address.latitude.toFloat(), address.longitude.toFloat())
        )
    }

}

data class OfferOperations(
    val setTitle: (String) -> Unit,
    val setDescription: (String) -> Unit
)