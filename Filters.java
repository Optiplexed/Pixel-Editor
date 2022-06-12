
import java.awt.*;
import java.awt.image.BufferedImage;

public class Filters {

    public static BufferedImage grayscale(BufferedImage img)
        {
        BufferedImage new_img = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int i = 0; i < img.getHeight(); ++i)
            {
            for (int j = 0; j < img.getWidth(); ++j)
                {
                int color = img.getRGB(j, i);
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = (color) & 0xFF;

                int avg = (r + g + b) / 3;
                new_img.setRGB(j, i, new Color(avg, avg, avg).getRGB());
                }
            }
        return new_img;
        }

    public static BufferedImage blur(BufferedImage img, int kernel_size)
        {
        BufferedImage new_img = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);

        int kernel[][] = new int[kernel_size][kernel_size];
        for (int i = 0; i < kernel_size; ++i)
            {
            for (int j = 0; j < kernel_size; ++j)
                {
                kernel[i][j] = 1;
                }
            }

        for (int y = 0; y < img.getHeight(); ++y)
            {
            for (int x = 0; x < img.getWidth(); ++x)
                {
                int weight = kernel_size * kernel_size;
                int sumr = 0;
                int sumg = 0;
                int sumb = 0;

                for (int i = -1 * (kernel_size / 2); i <= kernel_size / 2; ++i)
                    {
                    for (int j = -1 * (kernel_size / 2); j <= kernel_size / 2; ++j)
                        {
                            if ((x + i >= 0 && x + i < img.getWidth()) && (y + j >= 0 && y + j < img.getHeight())) {
                                int color = img.getRGB(x + i, y + j);
                                sumr += (color >> 16) & 0xFF;
                                sumg += (color >> 8) & 0xFF;
                                sumb += (color) & 0xFF;
                            }
                        }
                    }
                new_img.setRGB(x, y, new Color(sumr / weight, sumg / weight, sumb /weight).getRGB());
                }
            }
        return new_img;
        }

    public static BufferedImage sepia(BufferedImage img)
    {
        BufferedImage new_img = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int i = 0; i < img.getHeight(); ++i)
        {
            for (int j = 0; j < img.getWidth(); ++j)
            {
                int color = img.getRGB(j, i);
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = (color) & 0xFF;

                int newr = (int)(0.393 * r + 0.769 * g + 0.189 * b);
                int newg = (int)(0.349 * r + 0.686 * g + 0.168 * b);
                int newb = (int)(0.272 * r + 0.534 * g + 0.131 * b);

                if (newr > 255) newr = 255;
                if (newg > 255) newg = 255;
                if (newb > 255) newb = 255;

                try {
                    new_img.setRGB(j, i, new Color(newr, newg, newb).getRGB());
                }
                catch(IllegalArgumentException ex) {

                }
            }
        }
        return new_img;
    }
}
