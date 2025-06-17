import React from "react";
import { Sankey, sankeyCenter, SankeyNode, SankeyLink } from "@visx/sankey";
import { Group } from "@visx/group";
import { scaleOrdinal } from "d3-scale";
import { MedicinalUnitMode, MedicinalUnitModeUnits } from "../../types/MedicinalUnitMode";
import { Box, Typography } from "@mui/material";

type NodeDatum = { id: string; label: string };
type LinkDatum = { source: string; target: string; value: number };

interface SankeyChartProps {
    nodes: NodeDatum[];
    links: LinkDatum[];
    medicinalUnitMode: MedicinalUnitMode;
    height?: number;
}

export const SankeyChart: React.FC<SankeyChartProps> = ({
                                                            nodes,
                                                            links,
                                                            medicinalUnitMode,
                                                            height = 300
                                                        }) => {
    if (!nodes || nodes.length === 0 || !links || links.length === 0) {
        return (
            <Box sx={{ width: '100%', textAlign: 'center', p: 2 }}>
                <Typography variant="body1" color="textSecondary">
                    Žádná data k zobrazení.
                </Typography>
            </Box>
        );
    }

    const color = scaleOrdinal<string, string>()
        .domain(nodes.map(n => n.label))
        .range(["#34558a", "#4f6da2", "#6c88b8", "#8aa2cb", "#abc", "#ddd"]);

    const unitWord = MedicinalUnitModeUnits[medicinalUnitMode];

    const graph = { nodes, links };

    // Dynamically increase height based on number of nodes to avoid compression
    const dynamicHeight = Math.max(height, nodes.length * 40);

    return (
        <Box sx={{ width: '100%', overflowX: 'auto' }}>
            <Box sx={{ minWidth: '1000px' }}>
                <svg width="1000" height={dynamicHeight}>
                    <Sankey<NodeDatum, LinkDatum>
                        root={graph}
                        size={[1000 - 80, dynamicHeight - 48]}
                        nodeAlign={sankeyCenter}
                        nodeWidth={20}
                        nodePadding={40}
                        nodeId={(d) => d.id}
                    >
                        {({ graph, createPath }) => (
                            <Group top={32} left={32}>
                                {graph.links.map((link, i) => {
                                    const sourceLabel =
                                        typeof link.source === "object"
                                            ? (link.source as SankeyNode<NodeDatum, LinkDatum>).label
                                            : String(link.source);

                                    const targetLabel =
                                        typeof link.target === "object"
                                            ? (link.target as SankeyNode<NodeDatum, LinkDatum>).label
                                            : String(link.target);

                                    const pathD = createPath(link);
                                    if (!pathD) return null;

                                    const rawWidth = link.width ?? 1;
                                    const MIN_WIDTH = 3;
                                    const strokeWidth = Math.max(MIN_WIDTH, rawWidth);

                                    const sourceX = (link.source as SankeyNode<NodeDatum, LinkDatum>).x1 ?? 0;
                                    const sourceY = (link.y0 ?? 0) + ((link.y1 ?? 0) - (link.y0 ?? 0)) / 2;
                                    const targetX = (link.target as SankeyNode<NodeDatum, LinkDatum>).x0 ?? 0;
                                    const targetY = (link.y0 ?? 0) + ((link.y1 ?? 0) - (link.y0 ?? 0)) / 2;

                                    const midX = (sourceX + targetX) / 2;
                                    const midY = (sourceY + targetY) / 2;

                                    return (
                                        <g key={`link-${i}`}>
                                            <path
                                                d={pathD}
                                                fill="none"
                                                stroke={color(targetLabel)}
                                                strokeWidth={strokeWidth}
                                                strokeOpacity={0.35}
                                            >
                                                <title>{`${sourceLabel} → ${targetLabel}: ${link.value} ${unitWord}`}</title>
                                            </path>

                                            {strokeWidth > 64 && (
                                                <text
                                                    x={midX}
                                                    y={midY}
                                                    dy="0.35em"
                                                    fontSize={10}
                                                    textAnchor="middle"
                                                    fill="#555"
                                                    pointerEvents="none"
                                                >
                                                    {`${sourceLabel} → ${targetLabel}: ${link.value} ${unitWord}`}
                                                </text>
                                            )}
                                        </g>
                                    );
                                })}

                                {graph.nodes.map((node, i) => {
                                    const x0 = node.x0 ?? 0;
                                    const x1 = node.x1 ?? 0;
                                    const y0 = node.y0 ?? 0;
                                    const y1 = node.y1 ?? 0;
                                    const label = node.label;

                                    return (
                                        <Group key={`node-${i}`}>
                                            <rect
                                                x={x0}
                                                y={y0}
                                                width={x1 - x0}
                                                height={y1 - y0}
                                                fill={color(label)}
                                                rx={4}
                                            />
                                            <text
                                                x={(x0 + x1) / 2}
                                                y={y0 - 8}
                                                textAnchor="middle"
                                                fontSize={12}
                                                fill="#333"
                                            >
                                                {label}
                                            </text>
                                        </Group>
                                    );
                                })}
                            </Group>
                        )}
                    </Sankey>
                </svg>
            </Box>
        </Box>
    );
};
