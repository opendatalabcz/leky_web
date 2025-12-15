// src/components/SiteFooter.tsx

import React from "react"
import { Box, Typography, Link } from "@mui/material"

export const SiteFooter: React.FC = () => (
    <Box mt={6} py={4} px={2} bgcolor="#f5f5f5" textAlign="center">
        <Typography variant="body2" color="text.secondary" mb={1}>
            © <Link href="https://opendatalab.cz/" target="_blank" rel="noopener noreferrer" underline="hover">
            OpenDataLab
        </Link>{" "}
            2025,{" "}
            <Link href="https://www.linkedin.com/in/jaroslav-machovec-a0834011a/" target="_blank" rel="noopener noreferrer" underline="hover">
                Jaroslav Machovec
            </Link>. Portál vznikl jako diplomová práce na FIT ČVUT.
        </Typography>
        <Typography variant="caption" color="text.secondary">
            Data jsou využívána v souladu s{" "}
            <Link href="https://opendata.sukl.cz/?q=podminky-uziti-otevrenych-dat" target="_blank" rel="noopener noreferrer" underline="hover">
                podmínkami užití otevřených dat SÚKL
            </Link>. SÚKL nenese odpovědnost za způsob jejich dalšího zpracování.
        </Typography>
    </Box>
)
