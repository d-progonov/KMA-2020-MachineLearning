import javax.imageio.ImageIO
import javax.imageio.ImageReadParam
import javax.imageio.ImageTypeSpecifier
import java.awt.image.BufferedImage

class ImageReader {

    static int[][] readGreenChannel(File file) {
        def stream = ImageIO.createImageInputStream(new FileInputStream(file))

        Exception lastException = null
        BufferedImage image
        for (def reader : ImageIO.getImageReaders(stream)) {
            try {
                ImageReadParam param = reader.getDefaultReadParam()
                reader.setInput(stream, true, true)
                Iterator<ImageTypeSpecifier> imageTypes = reader.getImageTypes(0)
                while (imageTypes.hasNext()) {
                    ImageTypeSpecifier imageTypeSpecifier = imageTypes.next()
                    int bufferedImageType = imageTypeSpecifier.getBufferedImageType()
                    if (bufferedImageType == BufferedImage.TYPE_BYTE_GRAY) {
                        param.setDestinationType(imageTypeSpecifier)
                        break
                    }
                }
                image = reader.read(0, param)
                if (null != image) break
            } catch (Exception e) {
                lastException = e
            } finally {
                reader.dispose()
            }
        }

        if (null == image) {
            if (null != lastException) {
                throw lastException
            }
        }

        int width = image.getWidth()
        int height = image.getHeight()
        int[][] result = new int[height][width]

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                result[row][col] = (image.getRGB(col, row) >> 8) & 0xff
            }
        }

        result
    }
}
