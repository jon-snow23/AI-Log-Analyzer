import React from 'react';
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid, LabelList } from 'recharts';
import { truncate } from '../utils/formatters';
import ChartTooltip from './ChartTooltip';
import ChartCard from './ChartCard';

export default function ServiceErrorsChart({ data }) {
  return (
    <ChartCard
      eyebrow="Service impact"
      title="Error volume by service"
      subtitle="Relative concentration of warning and error traffic by inferred service or executable."
      delay={0.1}
    >
      <ResponsiveContainer width="100%" height={260}>
        <BarChart data={data} margin={{ top: 16, right: 16, bottom: 34, left: 0 }} barCategoryGap="18%">
          <CartesianGrid strokeDasharray="3 3" vertical={false} />
          <XAxis
            dataKey="name"
            interval={0}
            minTickGap={24}
            tickMargin={14}
            angle={-14}
            textAnchor="end"
            height={58}
            tickFormatter={(value) => truncate(value, 16)}
          />
          <YAxis allowDecimals={false} width={40} />
          <Tooltip
            cursor={{ fill: 'rgba(148, 163, 184, 0.16)' }}
            content={<ChartTooltip valueLabel="Entries" />}
            allowEscapeViewBox={{ x: true, y: true }}
            wrapperStyle={{ outline: 'none', zIndex: 20 }}
          />
          <Bar dataKey="count" fill="#2563eb" radius={[8, 8, 0, 0]} maxBarSize={72}>
            <LabelList dataKey="count" position="top" className="chart-value-label" />
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}
