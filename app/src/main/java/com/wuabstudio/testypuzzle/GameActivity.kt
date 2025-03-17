package com.wuabstudio.testypuzzle

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.button.MaterialButton
import com.wuabstudio.testypuzzle.api.PixabayImage
import com.wuabstudio.testypuzzle.repository.ImageRepository
import kotlinx.coroutines.launch
import kotlin.math.abs

import kotlin.random.Random

class GameActivity : AppCompatActivity() {
    private lateinit var puzzleView: PuzzleView
    private lateinit var resetButton: MaterialButton
    private lateinit var backButton: MaterialButton
    private lateinit var levelTextView: TextView
    private lateinit var categoryTextView: TextView
    private lateinit var progressBar: ProgressBar
    
    private var currentLevel = 1
    private var vibrator: Vibrator? = null
    private var vibrationEnabled = true
    private var currentCategory = "nature"
    
    private val imageRepository = ImageRepository()
    private var imageList: List<PixabayImage> = emptyList()
    private var currentImageIndex = 0
    
    private val TAG = "GameActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        puzzleView = findViewById(R.id.puzzleView)
        resetButton = findViewById(R.id.btnReset)
        backButton = findViewById(R.id.btnBack)
        levelTextView = findViewById(R.id.tvLevel)
        categoryTextView = findViewById(R.id.tvCategory)
        progressBar = findViewById(R.id.progressBar)
        
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        
        // Get vibration setting from preferences
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        vibrationEnabled = prefs.getBoolean("vibration", true)
        
        levelTextView.text = getString(R.string.level, currentLevel)
        
        resetButton.setOnClickListener {
            puzzleView.resetPuzzle()
        }
        
        backButton.setOnClickListener {
            finish()
        }
        
        // Callback when puzzle is completed
        puzzleView.setOnPuzzleCompletedListener {
            if (vibrationEnabled) {
                vibrator?.vibrate(500)
            }
            
            showLevelCompletedDialog()
        }
        
        // Load images from API
        loadImagesFromApi()
    }
    
    private fun loadImagesFromApi() {
        progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                // Get random category
                val categories = listOf("nature", "animals", "food", "travel", "architecture")
                currentCategory = categories[Random.nextInt(categories.size)]
                categoryTextView.text = "Category: ${currentCategory.capitalize()}"
                
                Log.d(TAG, "Loading images from API for category: $currentCategory")
                
                imageList = imageRepository.getRandomImages(currentCategory)
                
                if (imageList.isNotEmpty()) {
                    // Start the first level with the first image
                    Log.d(TAG, "Successfully loaded ${imageList.size} images")
                    startLevel(currentLevel)
                } else {
                    // Fallback to local images if API fails
                    Log.e(TAG, "API returned empty image list")
                    Toast.makeText(this@GameActivity, getString(R.string.error_loading_images), Toast.LENGTH_SHORT).show()
                    startLevelWithLocalImage(currentLevel)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during image loading", e)
                // Fallback to local images if API fails
                Toast.makeText(this@GameActivity, getString(R.string.error_loading_images), Toast.LENGTH_SHORT).show()
                startLevelWithLocalImage(currentLevel)
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun startLevel(level: Int) {
        // Set difficulty (number of pieces)
        val difficulty = when(level) {
            1 -> 3 // 3x3 puzzle
            2 -> 4 // 4x4 puzzle
            else -> 5 // 5x5 puzzle
        }
        
        if (imageList.isEmpty()) {
            Log.d(TAG, "Image list is empty, using local images")
            startLevelWithLocalImage(level)
            return
        }
        
        // Get image from the list
        currentImageIndex = (currentImageIndex + 1) % imageList.size
        val imageUrl = imageList[currentImageIndex].webformatURL
        
        Log.d(TAG, "Loading image from URL: $imageUrl")
        progressBar.visibility = View.VISIBLE
        
        // Load image with Glide
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Log.d(TAG, "Image loaded successfully, size: ${resource.width}x${resource.height}")
                    // Initialize puzzle with the loaded image
                    puzzleView.initializePuzzleWithBitmap(resource, difficulty)
                    progressBar.visibility = View.GONE
                }
                
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    Log.e(TAG, "Failed to load image from URL: $imageUrl")
                    super.onLoadFailed(errorDrawable)
                    // Fallback to local image
                    startLevelWithLocalImage(level)
                    progressBar.visibility = View.GONE
                }
                
                override fun onLoadCleared(placeholder: Drawable?) {
                    // Not used
                }
            })
        
        levelTextView.text = getString(R.string.level, level)
    }
    
    private fun startLevelWithLocalImage(level: Int) {
        Log.d(TAG, "Starting level with local image for level: $level")
        // Set difficulty (number of pieces)
        val difficulty = when(level) {
            1 -> 3 // 3x3 puzzle
            2 -> 4 // 4x4 puzzle
            else -> 5 // 5x5 puzzle
        }
        
        // Select image for the level
        val imageResId = when(level) {
            1 -> R.drawable.puzzle_image_1
            2 -> R.drawable.puzzle_image_1
            else -> R.drawable.puzzle_image_1
        }
        
        // Initialize puzzle
        puzzleView.initializePuzzle(imageResId, difficulty)
        levelTextView.text = getString(R.string.level, level)
        categoryTextView.text = "Category: Local Images"
    }
    
    private fun showLevelCompletedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.congratulations)
            .setMessage(R.string.level_completed)
            .setPositiveButton(R.string.next_level) { _, _ ->
                currentLevel++
                if (currentLevel > 3) {
                    currentLevel = 1 // Restart game
                    Toast.makeText(this, getString(R.string.all_levels_completed), Toast.LENGTH_LONG).show()
                    
                    // Load new images for a new game
                    loadImagesFromApi()
                } else {
                    startLevel(currentLevel)
                }
            }
            .setCancelable(false)
            .show()
    }
}

// String extension to capitalize first letter
private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

class PuzzleView(context: Context, attrs: android.util.AttributeSet? = null) : View(context, attrs) {
    private var bitmap: Bitmap? = null
    private var scaledBitmap: Bitmap? = null
    private var pieces = mutableListOf<PuzzlePiece>()
    private var xDivs = 3 // Default divisions
    private var yDivs = 3
    private var selectedPiece: PuzzlePiece? = null
    private var isInitialized = false
    private var onPuzzleCompletedListener: (() -> Unit)? = null
    
    // Paint for piece borders
    private val borderPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.colorPuzzleBorder)
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }
    
    // Paint for selected piece highlight
    private val selectedPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.colorPuzzleSelected)
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }
    
    // Paint for shadow
    private val shadowPaint = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.black)
        alpha = 80 // Semi-transparent
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    // Paint for correct position indicator
    private val correctPositionPaint = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.holo_green_light)
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    
    fun setOnPuzzleCompletedListener(listener: () -> Unit) {
        onPuzzleCompletedListener = listener
    }
    
    fun initializePuzzle(imageResId: Int, divisions: Int) {
        xDivs = divisions
        yDivs = divisions
        
        // Load image
        val options = BitmapFactory.Options()
        options.inScaled = false
        bitmap = BitmapFactory.decodeResource(resources, imageResId, options)
        
        // Scale bitmap to fit view
        scaleBitmapToFitView()
        
        createPuzzlePieces()
        isInitialized = true
        invalidate()
    }
    
    fun initializePuzzleWithBitmap(imageBitmap: Bitmap, divisions: Int) {
        xDivs = divisions
        yDivs = divisions
        
        // Use the provided bitmap
        bitmap = imageBitmap
        
        // Scale bitmap to fit view
        scaleBitmapToFitView()
        
        createPuzzlePieces()
        isInitialized = true
        invalidate()
    }
    
    private fun scaleBitmapToFitView() {
        bitmap?.let { bmp ->
            // Get view dimensions
            val viewWidth = width
            val viewHeight = height
            
            if (viewWidth <= 0 || viewHeight <= 0) {
                // View not yet measured, wait for onSizeChanged
                return
            }
            
            Log.d("PuzzleView", "Scaling bitmap from ${bmp.width}x${bmp.height} to fit view ${viewWidth}x${viewHeight}")
            
            // Calculate scale to fit view while maintaining aspect ratio
            val scaleWidth = viewWidth.toFloat() / bmp.width
            val scaleHeight = viewHeight.toFloat() / bmp.height
            val scale = minOf(scaleWidth, scaleHeight) * 0.95f // 95% to leave some margin
            
            // Create scaled bitmap
            val scaledWidth = (bmp.width * scale).toInt()
            val scaledHeight = (bmp.height * scale).toInt()
            
            Log.d("PuzzleView", "New scaled dimensions: ${scaledWidth}x${scaledHeight}")
            
            scaledBitmap = Bitmap.createScaledBitmap(bmp, scaledWidth, scaledHeight, true)
        }
    }
    
    fun resetPuzzle() {
        if (bitmap != null) {
            createPuzzlePieces()
            invalidate()
        }
    }
    
    private fun createPuzzlePieces() {
        pieces.clear()
        
        scaledBitmap?.let { bmp ->
            val pieceWidth = bmp.width / xDivs
            val pieceHeight = bmp.height / yDivs
            
            // Create all pieces
            for (y in 0 until yDivs) {
                for (x in 0 until xDivs) {
                    val pieceBitmap = Bitmap.createBitmap(
                        bmp,
                        x * pieceWidth,
                        y * pieceHeight,
                        pieceWidth,
                        pieceHeight
                    )
                    
                    val piece = PuzzlePiece(
                        pieceBitmap,
                        x,
                        y,
                        pieceWidth,
                        pieceHeight
                    )
                    
                    // Calculate center position of the view
                    val viewCenterX = width / 2
                    val viewCenterY = height / 2
                    
                    // Calculate puzzle center
                    val puzzleCenterX = bmp.width / 2
                    val puzzleCenterY = bmp.height / 2
                    
                    // Set correct positions centered in the view
                    piece.correctX = viewCenterX - puzzleCenterX + x * pieceWidth.toFloat()
                    piece.correctY = viewCenterY - puzzleCenterY + y * pieceHeight.toFloat()
                    
                    pieces.add(piece)
                }
            }
            
            // Shuffle pieces
            pieces.shuffle()
            
            // Arrange shuffled pieces on screen in a more distributed way
            val viewWidth = width.toFloat()
            val viewHeight = height.toFloat()
            
            // Calculate margins to distribute pieces evenly
            val horizontalMargin = (viewWidth - (pieceWidth * xDivs)) / (xDivs + 1)
            val verticalMargin = (viewHeight - (pieceHeight * yDivs)) / (yDivs + 1)
            
            Log.d("PuzzleView", "Distributing pieces with margins: h=$horizontalMargin, v=$verticalMargin")
            
            // If we have too many pieces or small screen, use a grid layout
            if (horizontalMargin < 10 || verticalMargin < 10) {
                // Fall back to grid layout
                for (i in pieces.indices) {
                    val row = i / xDivs
                    val col = i % xDivs
                    
                    pieces[i].x = col * pieceWidth.toFloat()
                    pieces[i].y = row * pieceHeight.toFloat()
                }
            } else {
                // Distribute pieces around the edges of the screen
                val totalPieces = pieces.size
                val piecesPerSide = totalPieces / 4 + 1
                
                for (i in pieces.indices) {
                    when {
                        // Top edge
                        i < piecesPerSide -> {
                            val spacing = viewWidth / (piecesPerSide + 1)
                            pieces[i].x = (i + 1) * spacing - pieceWidth / 2
                            pieces[i].y = verticalMargin
                        }
                        // Right edge
                        i < piecesPerSide * 2 -> {
                            val idx = i - piecesPerSide
                            val spacing = viewHeight / (piecesPerSide + 1)
                            pieces[i].x = viewWidth - pieceWidth - horizontalMargin
                            pieces[i].y = (idx + 1) * spacing - pieceHeight / 2
                        }
                        // Bottom edge
                        i < piecesPerSide * 3 -> {
                            val idx = i - (piecesPerSide * 2)
                            val spacing = viewWidth / (piecesPerSide + 1)
                            pieces[i].x = viewWidth - ((idx + 1) * spacing) - pieceWidth / 2
                            pieces[i].y = viewHeight - pieceHeight - verticalMargin
                        }
                        // Left edge
                        else -> {
                            val idx = i - (piecesPerSide * 3)
                            val spacing = viewHeight / (piecesPerSide + 1)
                            pieces[i].x = horizontalMargin
                            pieces[i].y = viewHeight - ((idx + 1) * spacing) - pieceHeight / 2
                        }
                    }
                    
                    // Ensure pieces are within view bounds
                    pieces[i].x = pieces[i].x.coerceIn(0f, viewWidth - pieceWidth)
                    pieces[i].y = pieces[i].y.coerceIn(0f, viewHeight - pieceHeight)
                }
            }
        }
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        Log.d("PuzzleView", "onSizeChanged: $w x $h")
        
        if (bitmap != null) {
            scaleBitmapToFitView()
            
            if (!isInitialized) {
                createPuzzlePieces()
                isInitialized = true
            } else {
                // Recalculate piece positions based on new view size
                scaledBitmap?.let { bmp ->
                    val pieceWidth = bmp.width / xDivs
                    val pieceHeight = bmp.height / yDivs
                    
                    // Calculate center position of the view
                    val viewCenterX = width / 2
                    val viewCenterY = height / 2
                    
                    // Calculate puzzle center
                    val puzzleCenterX = bmp.width / 2
                    val puzzleCenterY = bmp.height / 2
                    
                    for (piece in pieces) {
                        val x = piece.originalX
                        val y = piece.originalY
                        
                        // Update correct positions centered in the view
                        piece.correctX = viewCenterX - puzzleCenterX + x * pieceWidth.toFloat()
                        piece.correctY = viewCenterY - puzzleCenterY + y * pieceHeight.toFloat()
                    }
                }
            }
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (pieces.isEmpty()) return
        
        // Draw pieces from bottom to top (first to last)
        for (i in 0 until pieces.size) {
            val piece = pieces[i]
            
            // Skip the selected piece, we'll draw it last
            if (piece == selectedPiece) continue
            
            // Draw shadow for 3D effect
            canvas.drawRect(
                piece.x + 3f,
                piece.y + 3f,
                piece.x + piece.width + 3f,
                piece.y + piece.height + 3f,
                shadowPaint
            )
            
            // Draw piece
            canvas.drawBitmap(piece.bitmap, piece.x, piece.y, null)
            
            // Draw border
            canvas.drawRect(
                piece.x,
                piece.y,
                piece.x + piece.width,
                piece.y + piece.height,
                borderPaint
            )
            
            // If piece is in correct position, show a green indicator
            if (piece.isPlaced) {
                canvas.drawRect(
                    piece.x,
                    piece.y,
                    piece.x + piece.width,
                    piece.y + piece.height,
                    correctPositionPaint
                )
            }
        }
        
        // Draw selected piece last (on top)
        selectedPiece?.let { piece ->
            // Draw shadow for 3D effect (larger for selected piece)
            canvas.drawRect(
                piece.x + 5f,
                piece.y + 5f,
                piece.x + piece.width + 5f,
                piece.y + piece.height + 5f,
                shadowPaint
            )
            
            // Draw piece
            canvas.drawBitmap(piece.bitmap, piece.x, piece.y, null)
            
            // Draw highlight
            canvas.drawRect(
                piece.x,
                piece.y,
                piece.x + piece.width,
                piece.y + piece.height,
                selectedPaint
            )
        }
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                selectedPiece = getPieceAt(x, y)
                selectedPiece?.let {
                    // Bring selected piece to front
                    pieces.remove(it)
                    pieces.add(it)
                }
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                selectedPiece?.let {
                    // Center piece under finger
                    it.x = x - it.width / 2
                    it.y = y - it.height / 2
                    
                    // Keep piece within view bounds
                    it.x = it.x.coerceIn(0f, width - it.width.toFloat())
                    it.y = it.y.coerceIn(0f, height - it.height.toFloat())
                    
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                selectedPiece?.let {
                    // Snap piece to correct position if close enough
                    if (abs(it.x - it.correctX) < it.width / 4 && 
                        abs(it.y - it.correctY) < it.height / 4) {
                        it.x = it.correctX
                        it.y = it.correctY
                        it.isPlaced = true
                    }
                    
                    // Check if puzzle is completed
                    if (isPuzzleCompleted()) {
                        onPuzzleCompletedListener?.invoke()
                    }
                }
                selectedPiece = null
                invalidate()
            }
        }
        
        return true
    }
    
    private fun getPieceAt(x: Float, y: Float): PuzzlePiece? {
        // Check pieces from top to bottom (last to first)
        for (i in pieces.size - 1 downTo 0) {
            val piece = pieces[i]
            if (x >= piece.x && x <= piece.x + piece.width &&
                y >= piece.y && y <= piece.y + piece.height) {
                return piece
            }
        }
        return null
    }
    
    private fun isPuzzleCompleted(): Boolean {
        return pieces.all { it.isPlaced }
    }
}

data class PuzzlePiece(
    val bitmap: Bitmap,
    val originalX: Int,
    val originalY: Int,
    val width: Int,
    val height: Int
) {
    var x: Float = 0f
    var y: Float = 0f
    var correctX: Float = 0f
    var correctY: Float = 0f
    var isPlaced: Boolean = false
        get() = abs(x - correctX) < 5 && abs(y - correctY) < 5
} 